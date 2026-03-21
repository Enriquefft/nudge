package middleware

import (
	"fmt"
	"net/http"
	"sync"
	"time"
)

// bucket tracks request counts within a time window for a single merchant.
type bucket struct {
	mu       sync.Mutex
	tokens   int
	limit    int
	lastFill time.Time
}

func (b *bucket) allow() bool {
	b.mu.Lock()
	defer b.mu.Unlock()

	now := time.Now()
	elapsed := now.Sub(b.lastFill)

	// Refill tokens proportionally to elapsed time.
	if elapsed >= time.Minute {
		b.tokens = b.limit
		b.lastFill = now
	} else {
		refill := int(elapsed.Seconds() * float64(b.limit) / 60.0)
		if refill > 0 {
			b.tokens += refill
			if b.tokens > b.limit {
				b.tokens = b.limit
			}
			b.lastFill = now
		}
	}

	if b.tokens <= 0 {
		return false
	}

	b.tokens--
	return true
}

// RateLimiter implements per-merchant token bucket rate limiting.
type RateLimiter struct {
	buckets      sync.Map
	defaultLimit int
}

// NewRateLimiter creates a new rate limiter with the given default requests-per-minute limit.
func NewRateLimiter(defaultLimit int) *RateLimiter {
	rl := &RateLimiter{
		defaultLimit: defaultLimit,
	}

	// Periodically clean up stale buckets.
	go func() {
		ticker := time.NewTicker(5 * time.Minute)
		defer ticker.Stop()
		for range ticker.C {
			rl.buckets.Range(func(key, value any) bool {
				b := value.(*bucket)
				b.mu.Lock()
				if time.Since(b.lastFill) > 10*time.Minute {
					rl.buckets.Delete(key)
				}
				b.mu.Unlock()
				return true
			})
		}
	}()

	return rl
}

// Middleware returns an HTTP middleware that enforces per-merchant rate limits.
func (rl *RateLimiter) Middleware() func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			merchantID := MerchantIDFrom(r.Context())
			if merchantID == "" {
				// Not authenticated yet (public endpoints); skip rate limiting.
				next.ServeHTTP(w, r)
				return
			}

			b := rl.getBucket(merchantID)
			if !b.allow() {
				w.Header().Set("Content-Type", "application/json")
				w.Header().Set("Retry-After", "60")
				w.WriteHeader(http.StatusTooManyRequests)
				_, _ = fmt.Fprintf(w, `{"error":"rate limit exceeded, retry after 60s"}`)
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}

// SetLimit updates the rate limit for a specific merchant (called when config is loaded).
func (rl *RateLimiter) SetLimit(merchantID string, limit int) {
	val, loaded := rl.buckets.LoadOrStore(merchantID, &bucket{
		tokens:   limit,
		limit:    limit,
		lastFill: time.Now(),
	})
	if loaded {
		b := val.(*bucket)
		b.mu.Lock()
		b.limit = limit
		b.mu.Unlock()
	}
}

func (rl *RateLimiter) getBucket(merchantID string) *bucket {
	val, loaded := rl.buckets.LoadOrStore(merchantID, &bucket{
		tokens:   rl.defaultLimit,
		limit:    rl.defaultLimit,
		lastFill: time.Now(),
	})
	if !loaded {
		return val.(*bucket)
	}
	return val.(*bucket)
}
