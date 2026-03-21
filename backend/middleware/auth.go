package middleware

import (
	"context"
	"log/slog"
	"net/http"
	"strings"

	"github.com/Enriquefft/nudge/backend/internal/db"
)

// contextKey is an unexported type for context keys to prevent collisions.
type contextKey int

const (
	// DeviceIDKey is the context key for the authenticated device ID.
	DeviceIDKey contextKey = iota
	// MerchantIDKey is the context key for the authenticated merchant ID.
	MerchantIDKey
)

// DeviceIDFrom extracts the device ID from the request context.
func DeviceIDFrom(ctx context.Context) string {
	v, _ := ctx.Value(DeviceIDKey).(string)
	return v
}

// MerchantIDFrom extracts the merchant ID from the request context.
func MerchantIDFrom(ctx context.Context) string {
	v, _ := ctx.Value(MerchantIDKey).(string)
	return v
}

// Auth returns middleware that validates Bearer tokens against the devices table.
func Auth(pool *db.Pool) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			// Skip auth for public endpoints.
			path := r.URL.Path
			if path == "/api/register" || path == "/health" {
				next.ServeHTTP(w, r)
				return
			}

			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				writeError(w, http.StatusUnauthorized, "missing authorization header")
				return
			}

			token, found := strings.CutPrefix(authHeader, "Bearer ")
			if !found || token == "" {
				writeError(w, http.StatusUnauthorized, "invalid authorization format")
				return
			}

			var deviceID, merchantID string
			err := pool.QueryRow(r.Context(),
				`SELECT d.id, d.merchant_id FROM devices d WHERE d.api_token = $1`,
				token,
			).Scan(&deviceID, &merchantID)
			if err != nil {
				slog.Warn("auth failed", "error", err)
				writeError(w, http.StatusUnauthorized, "invalid api token")
				return
			}

			// Update last_seen_at asynchronously — fire and forget.
			go func() {
				_, err := pool.Exec(context.Background(),
					`UPDATE devices SET last_seen_at = NOW() WHERE id = $1`, deviceID)
				if err != nil {
					slog.Warn("failed to update last_seen_at", "device_id", deviceID, "error", err)
				}
			}()

			ctx := context.WithValue(r.Context(), DeviceIDKey, deviceID)
			ctx = context.WithValue(ctx, MerchantIDKey, merchantID)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func writeError(w http.ResponseWriter, status int, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	// Using fmt.Fprintf is fine here — the message is controlled by us, not user input.
	_, _ = w.Write([]byte(`{"error":"` + msg + `"}`))
}
