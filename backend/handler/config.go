package handler

import (
	"encoding/json"
	"log/slog"
	"net/http"

	"github.com/Enriquefft/nudge/backend/internal/db"
	"github.com/Enriquefft/nudge/backend/middleware"
)

type configResponse struct {
	AIModel              string          `json:"ai_model"`
	FeatureFlags         json.RawMessage `json:"feature_flags"`
	SystemPromptOverride *string         `json:"system_prompt_override"`
}

// Config handles GET /api/config — returns merchant remote configuration.
func Config(pool *db.Pool) http.HandlerFunc {
	defaultFlags := json.RawMessage(`{}`)

	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			respondError(w, http.StatusMethodNotAllowed, "method not allowed")
			return
		}

		merchantID := middleware.MerchantIDFrom(r.Context())

		var resp configResponse
		var featureFlags []byte

		err := pool.QueryRow(r.Context(),
			`SELECT ai_model, feature_flags, system_prompt_override
			 FROM merchant_config WHERE merchant_id = $1`,
			merchantID,
		).Scan(&resp.AIModel, &featureFlags, &resp.SystemPromptOverride)
		if err != nil {
			// Return defaults if no config exists.
			slog.Warn("no config found, returning defaults", "merchant_id", merchantID)
			respondJSON(w, http.StatusOK, configResponse{
				AIModel:              "glm-4.7-flash",
				FeatureFlags:         defaultFlags,
				SystemPromptOverride: nil,
			})
			return
		}

		if featureFlags != nil {
			resp.FeatureFlags = featureFlags
		} else {
			resp.FeatureFlags = defaultFlags
		}

		respondJSON(w, http.StatusOK, resp)
	}
}
