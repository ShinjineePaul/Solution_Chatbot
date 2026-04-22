const BASE = 'http://localhost:8080/api'


export async function sendMessage(query, history) {
  const res = await fetch(`${BASE}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, history })
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || 'Something went wrong')
  }
  return res.json()
}


export async function submitFeedback(userQuery, aiResponse, positive, comment = '') {
  const res = await fetch(`${BASE}/feedback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userQuery, aiResponse, positive, comment })
  })
  if (!res.ok) throw new Error('Failed to submit feedback')
  return res.text()
}