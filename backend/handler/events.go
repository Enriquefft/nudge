package handler

import (
	"encoding/json"
	"log/slog"
	"net/http"
	"time"

	"github.com/Enriquefft/nudge/backend/internal/db"
	"github.com/Enriquefft/nudge/backend/middleware"
)

type event struct {
	EventType       string    `json:"event_type"`
	TriggerItems    []string  `json:"trigger_items"`
	SuggestedItem   string    `json:"suggested_item"`
	SuggestedItemID string    `json:"suggested_item_id"`
	PriceCents      int64     `json:"price_cents"`
	AIModel         string    `json:"ai_model"`
	ResponseTimeMs  int       `json:"response_time_ms"`
	Timestamp       time.Time `json:"timestamp"`
}

type eventsRequest struct {
	Events []event `json:"events"`
}

type eventsResponse struct {
	Received int `json:"received"`
}

// Events handles POST /api/events — batch ingestion of suggestion analytics.
func Events(pool *db.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			respondError(w, http.StatusMethodNotAllowed, "method not allowed")
			return
		}

		var req eventsRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			respondError(w, http.StatusBadRequest, "invalid json body")
			return
		}

		if len(req.Events) == 0 {
			respondJSON(w, http.StatusOK, eventsResponse{Received: 0})
			return
		}

		deviceID := middleware.DeviceIDFrom(r.Context())
		merchantID := middleware.MerchantIDFrom(r.Context())

		// Validate event types before inserting.
		validTypes := map[string]bool{"shown": true, "accepted": true, "dismissed": true}
		for _, e := range req.Events {
			if !validTypes[e.EventType] {
				respondError(w, http.StatusBadRequest, "invalid event_type: "+e.EventType)
				return
			}
			if e.SuggestedItem == "" {
				respondError(w, http.StatusBadRequest, "suggested_item is required")
				return
			}
			if len(e.TriggerItems) == 0 {
				respondError(w, http.StatusBadRequest, "trigger_items is required")
				return
			}
		}

		ctx := r.Context()
		inserted := 0

		// Use a transaction for batch insert.
		tx, err := pool.Begin(ctx)
		if err != nil {
			slog.Error("failed to begin transaction", "error", err)
			respondError(w, http.StatusInternalServerError, "internal error")
			return
		}
		defer func() {
			if err := tx.Rollback(ctx); err != nil {
				// Rollback after commit returns an error; that's expected.
			}
		}()

		for _, e := range req.Events {
			_, err := tx.Exec(ctx,
				`INSERT INTO suggestion_events
				 (device_id, merchant_id, event_type, trigger_items, suggested_item,
				  suggested_item_id, price_cents, ai_model, response_time_ms)
				 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)`,
				deviceID, merchantID, e.EventType, e.TriggerItems, e.SuggestedItem,
				nilIfEmpty(e.SuggestedItemID), e.PriceCents, nilIfEmpty(e.AIModel),
				nilIfZero(e.ResponseTimeMs),
			)
			if err != nil {
				slog.Error("failed to insert event", "error", err, "event_type", e.EventType)
				respondError(w, http.StatusInternalServerError, "failed to store events")
				return
			}
			inserted++
		}

		if err := tx.Commit(ctx); err != nil {
			slog.Error("failed to commit events", "error", err)
			respondError(w, http.StatusInternalServerError, "failed to store events")
			return
		}

		slog.Info("events ingested",
			"count", inserted,
			"device_id", deviceID,
			"merchant_id", merchantID,
		)

		respondJSON(w, http.StatusOK, eventsResponse{Received: inserted})
	}
}

func nilIfEmpty(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

func nilIfZero(n int) *int {
	if n == 0 {
		return nil
	}
	return &n
}
