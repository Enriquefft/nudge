package ai

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"time"
)

// Proxy forwards chat completion requests to an AI provider.
type Proxy struct {
	client *http.Client
}

// NewProxy creates a new AI proxy with a configured HTTP client.
func NewProxy() *Proxy {
	return &Proxy{
		client: &http.Client{
			Timeout: 20 * time.Second,
		},
	}
}

// ForwardRequest sends the request body to the given AI provider endpoint and returns the raw response.
func (p *Proxy) ForwardRequest(ctx context.Context, baseURL, apiKey string, body []byte) ([]byte, int, error) {
	endpoint := baseURL + "/chat/completions"

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(body))
	if err != nil {
		return nil, 0, fmt.Errorf("create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+apiKey)

	start := time.Now()
	resp, err := p.client.Do(req)
	if err != nil {
		return nil, 0, fmt.Errorf("forward to ai provider: %w", err)
	}
	defer resp.Body.Close()

	elapsed := time.Since(start)
	slog.Info("ai request completed",
		"status", resp.StatusCode,
		"duration_ms", elapsed.Milliseconds(),
		"endpoint", endpoint,
	)

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, resp.StatusCode, fmt.Errorf("read ai response: %w", err)
	}

	return respBody, resp.StatusCode, nil
}
