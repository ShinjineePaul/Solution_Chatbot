import { useEffect, useRef, useState } from 'react'
import MessageBubble from './MessageBubble'
import TypingIndicator from './TypingIndicator'
import { sendMessage, submitFeedback } from '../services/api'

const SUGGESTIONS = [
  'rej 70 dispensing fee issue',
  'copay issue',
  'reprice issue',
  'rej MR ingredient issue'
]

export default function ChatWindow() {
  const [messages, setMessages]   = useState([])
  const [loading, setLoading]     = useState(false)
  const [error, setError]         = useState(null)
  const [input, setInput]         = useState('')
  const bottomRef                 = useRef(null)
  const loadingRef                = useRef(false)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const buildHistory = (msgs) =>
    msgs.map(m => ({ role: m.role, content: m.content }))

  const send = async (text) => {
    const q = text.trim()
    if (!q || loadingRef.current) return

    setError(null)
    setInput('')
    loadingRef.current = true
    setLoading(true)

    const userMsg = { role: 'user', content: q }
    const updatedMessages = [...messages, userMsg]
    setMessages(updatedMessages)

    try {
      const data = await sendMessage(q, buildHistory(updatedMessages))
      loadingRef.current = false
      setLoading(false)
      setMessages(prev => [
        ...prev,
        {
          role             : 'assistant',
          content          : data.answer,
          tickets          : data.matchedTickets    || [],
          keywords         : data.extractedKeywords || [],
          feedbackSent     : false,
          feedbackPositive : null
        }
      ])
    } catch (e) {
      loadingRef.current = false
      setLoading(false)
      setError(e.message || 'Something went wrong. Please try again.')
    }
  }

  const handleFeedback = async (msgIndex, positive) => {
    const aiMsg   = messages[msgIndex]
    const userMsg = messages[msgIndex - 1]
    if (!aiMsg || !userMsg) return
    try {
      await submitFeedback(userMsg.content, aiMsg.content, positive)
      setMessages(prev => prev.map((m, i) =>
        i === msgIndex
          ? { ...m, feedbackSent: true, feedbackPositive: positive }
          : m
      ))
    } catch {
      // feedback failure is non-critical
    }
  }

  const handleClear = () => {
    setMessages([])
    setError(null)
    setInput('')
    loadingRef.current = false
    setLoading(false)
  }

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send(input)
    }
  }

  return (
    <div className="chat-container">

      <div className="chat-header">
        <div className="header-left">
          <img src="/robot.png" alt="PBM PSO Chatbot" style={{ width: '40px', height: '40px' }} />
          <div>
            <h1>PBM PSO Chatbot</h1>
          </div>
        </div>
        {messages.length > 0 && (
          <button className="clear-btn" onClick={handleClear}>🗑 Clear</button>
        )}
      </div>

      <div className="chat-messages">
        {messages.length === 0 && !loading && (
          <div className="empty-state">
            <p>💬 Describe your PBM issue and I'll find resolution steps from past IMT TEAM tickets.</p>
            <div className="suggestions">
              {SUGGESTIONS.map(s => (
                <button key={s} className="suggestion-chip" onClick={() => send(s)}>{s}</button>
              ))}
            </div>
          </div>
        )}

        {messages.map((msg, i) => (
          <MessageBubble key={i} msg={msg} index={i} onFeedback={handleFeedback} />
        ))}

        {loading && <TypingIndicator />}

        {error && <div className="error-banner">⚠️ {error}</div>}

        <div ref={bottomRef} />
      </div>

      <div className="chat-input-row">
        <textarea
          className="chat-input"
          rows={2}
          placeholder="Describe your issue... (Enter to send, Shift+Enter for new line)"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKey}
        />
        <button
          className="send-btn"
          onClick={() => send(input)}
          disabled={!input.trim() || loading}
        >
          {loading ? '⏳' : 'Send ➤'}
        </button>
      </div>

    </div>
  )
}