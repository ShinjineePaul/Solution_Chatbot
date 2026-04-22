import FeedbackButtons from './FeedbackButtons'
import TicketSourcePanel from './TicketSourcePanel'

function renderMarkdown(text) {
  if (!text) return null
  return text.split('\n').map((line, i) => {
    const numMatch = line.match(/^(\s*)(\d+[\d.]*)\.\s+(.*)/)
    if (numMatch) {
      return (
        <div key={i} style={{ marginLeft: numMatch[1].length * 8 + 'px', margin: '2px 0' }}>
          <span style={{ fontWeight: 600, marginRight: 6 }}>{numMatch[2]}.</span>
          {inlineFormat(numMatch[3])}
        </div>
      )
    }
    const bulletMatch = line.match(/^(\s*)[*\-]\s+(.*)/)
    if (bulletMatch) {
      return (
        <div key={i} style={{ marginLeft: bulletMatch[1].length * 8 + 12 + 'px', margin: '2px 0' }}>
          <span style={{ marginRight: 6 }}>•</span>
          {inlineFormat(bulletMatch[2])}
        </div>
      )
    }
    if (line.trim() === '') return <div key={i} style={{ height: 8 }} />
    return <div key={i} style={{ margin: '2px 0' }}>{inlineFormat(line)}</div>
  })
}

function inlineFormat(text) {
  const parts = []
  const re = /\*\*(.*?)\*\*|\*(.*?)\*/g
  let last = 0, m
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) parts.push(text.slice(last, m.index))
    if (m[1] !== undefined) parts.push(<strong key={m.index}>{m[1]}</strong>)
    else parts.push(<em key={m.index}>{m[2]}</em>)
    last = m.index + m[0].length
  }
  if (last < text.length) parts.push(text.slice(last))
  return parts
}

export default function MessageBubble({ msg, index, onFeedback }) {
  const isUser = msg.role === 'user'

  return (
    <div className={`bubble-wrap ${isUser ? 'user-wrap' : 'bot-wrap'}`}>

      {!isUser && <div className="avatar-circle bot-avatar"></div>}

      <div className={`bubble ${isUser ? 'bubble-user' : 'bubble-bot'}`}>
        <div className="bubble-body">
          <div className="bubble-text">
            {isUser ? msg.content : renderMarkdown(msg.content)}
          </div>
          {!isUser && (
            <>
              <TicketSourcePanel tickets={msg.tickets} keywords={msg.keywords} />
              <FeedbackButtons
                msgIndex={index}
                feedbackSent={msg.feedbackSent}
                feedbackPositive={msg.feedbackPositive}
                onFeedback={onFeedback}
              />
            </>
          )}
        </div>
      </div>

      {isUser && <div className="avatar-circle user-avatar">👤</div>}

    </div>
  )
}