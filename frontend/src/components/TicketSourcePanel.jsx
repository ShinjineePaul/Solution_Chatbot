import { useState } from 'react'

export default function TicketSourcePanel({ tickets, keywords }) {
  const [open, setOpen] = useState(false)

  if (!tickets || tickets.length === 0) return null

  return (
    <div className="ticket-panel">
      <button
        className="ticket-toggle"
        onClick={() => setOpen(o => !o)}
      >
        📂 {open ? 'Hide' : 'Show'} {tickets.length} source ticket{tickets.length > 1 ? 's' : ''}
        {keywords?.length > 0 && (
          <span className="keyword-chips">
            {keywords.slice(0, 4).map(k => (
              <span key={k} className="keyword-chip">{k}</span>
            ))}
          </span>
        )}
      </button>

      {open && (
        <div className="ticket-list">
          {tickets.map(t => (
            <div key={t.ticketNo} className="ticket-card">
              <div className="ticket-header">
                <span className="ticket-id">{t.ticketNo}</span>
                <span className="ticket-sl">SL #{t.slNo}</span>
                <span className="ticket-status">{t.status}</span>
              </div>
              <div className="ticket-desc">{t.description}</div>
              <details>
                <summary>Resolution Notes</summary>
                <pre className="ticket-notes">{t.notes}</pre>
              </details>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}