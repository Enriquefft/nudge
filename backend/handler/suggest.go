package handler

import (
	"io"
	"log/slog"
	"net/http"
	"os"

	"github.com/Enriquefft/nudge/backend/internal/ai"
	"github.com/Enriquefft/nudge/backend/internal/db"
	"github.com/Enriquefft/nudge/backend/middleware"
)

// Suggest handles POST /api/suggest — proxies AI completion requests.
func Suggest(pool *db.Pool, proxy *ai.Proxy) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			respondError(w, http.StatusMethodNotAllowed, "method not allowed")
			return
		}

		merchantID := middleware.MerchantIDFrom(r.Context())

		// Read request body.
		body, err := io.ReadAll(r.Body)
		if err != nil {
			respondError(w, http.StatusBadRequest, "failed to read request body")
			return
		}

		// Look up merchant config for AI provider settings.
		var baseURL, apiKey string
		var configAPIKey *string
		err = pool.QueryRow(r.Context(),
			`SELECT ai_base_url, ai_api_key FROM merchant_config WHERE merchant_id = $1`,
			merchantID,
		).Scan(&baseURL, &configAPIKey)
		if err != nil {
			// Fall back to defaults if no config found.
			baseURL = "https://api.z.ai/api/paas/v4"
			slog.Warn("no merchant config found, using defaults", "merchant_id", merchantID)
		}

		// Use per-merchant key if set, otherwise fall back to global env var.
		if configAPIKey != nil && *configAPIKey != "" {
			apiKey = *configAPIKey
		} else {
			apiKey = os.Getenv("DEFAULT_AI_API_KEY")
		}

		if apiKey == "" {
			respondError(w, http.StatusServiceUnavailable, "ai api key not configured")
			return
		}

		// Forward the request to the AI provider.
		respBody, statusCode, err := proxy.ForwardRequest(r.Context(), baseURL, apiKey, body)
		if err != nil {
			slog.Error("ai proxy error", "error", err, "merchant_id", merchantID)
			respondError(w, http.StatusBadGateway, "ai provider request failed")
			return
		}

		// Return the AI response verbatim.
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(statusCode)
		_, err = w.Write(respBody)
		if err != nil {
			slog.Error("failed to write response", "error", err)
		}
	}
}
