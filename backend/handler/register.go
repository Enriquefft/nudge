package handler

import (
	"encoding/json"
	"log/slog"
	"net/http"

	"github.com/Enriquefft/nudge/backend/internal/db"
)

type registerRequest struct {
	DeviceID     string `json:"device_id"`
	MerchantName string `json:"merchant_name"`
	AppVersion   string `json:"app_version"`
	Platform     string `json:"platform"`
}

type registerResponse struct {
	APIToken   string `json:"api_token"`
	MerchantID string `json:"merchant_id"`
}

// Register handles POST /api/register — device registration and merchant creation.
func Register(pool *db.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			respondError(w, http.StatusMethodNotAllowed, "method not allowed")
			return
		}

		var req registerRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			respondError(w, http.StatusBadRequest, "invalid json body")
			return
		}

		if req.DeviceID == "" {
			respondError(w, http.StatusBadRequest, "device_id is required")
			return
		}
		if req.MerchantName == "" {
			respondError(w, http.StatusBadRequest, "merchant_name is required")
			return
		}
		if req.Platform == "" {
			req.Platform = "android"
		}

		ctx := r.Context()

		// Check if device already exists.
		var existingToken, existingMerchantID string
		err := pool.QueryRow(ctx,
			`SELECT api_token, merchant_id FROM devices WHERE id = $1`,
			req.DeviceID,
		).Scan(&existingToken, &existingMerchantID)
		if err == nil {
			// Device already registered — return existing credentials.
			slog.Info("device already registered", "device_id", req.DeviceID)
			respondJSON(w, http.StatusOK, registerResponse{
				APIToken:   existingToken,
				MerchantID: existingMerchantID,
			})
			return
		}

		// Try to find existing merchant by name.
		var merchantID string
		err = pool.QueryRow(ctx,
			`SELECT id FROM merchants WHERE name = $1`,
			req.MerchantName,
		).Scan(&merchantID)
		if err != nil {
			// Merchant doesn't exist — create it.
			err = pool.QueryRow(ctx,
				`INSERT INTO merchants (name) VALUES ($1) RETURNING id`,
				req.MerchantName,
			).Scan(&merchantID)
			if err != nil {
				slog.Error("failed to create merchant", "error", err)
				respondError(w, http.StatusInternalServerError, "failed to create merchant")
				return
			}

			// Create default config for new merchant.
			_, err = pool.Exec(ctx,
				`INSERT INTO merchant_config (merchant_id) VALUES ($1) ON CONFLICT DO NOTHING`,
				merchantID,
			)
			if err != nil {
				slog.Error("failed to create merchant config", "error", err)
				// Non-fatal — continue with registration.
			}

			slog.Info("created new merchant", "merchant_id", merchantID, "name", req.MerchantName)
		}

		// Create device and get the generated token.
		var apiToken string
		err = pool.QueryRow(ctx,
			`INSERT INTO devices (id, merchant_id, platform, app_version)
			 VALUES ($1, $2, $3, $4)
			 RETURNING api_token`,
			req.DeviceID, merchantID, req.Platform, req.AppVersion,
		).Scan(&apiToken)
		if err != nil {
			slog.Error("failed to create device", "error", err)
			respondError(w, http.StatusInternalServerError, "failed to register device")
			return
		}

		slog.Info("registered new device",
			"device_id", req.DeviceID,
			"merchant_id", merchantID,
		)

		respondJSON(w, http.StatusCreated, registerResponse{
			APIToken:   apiToken,
			MerchantID: merchantID,
		})
	}
}
