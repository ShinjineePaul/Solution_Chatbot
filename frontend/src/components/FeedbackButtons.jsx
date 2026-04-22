export default function FeedbackButtons({ msgIndex, feedbackSent, feedbackPositive, onFeedback }) {
  if (feedbackSent) {
    return (
      <div className="feedback-row">
        <span className="feedback-thanks">
          {feedbackPositive ? '👍 Thanks for the feedback!' : '👎 Thanks, we\'ll improve!'}
        </span>
      </div>
    )
  }

  return (
    <div className="feedback-row">
      <span className="feedback-label">Was this helpful?</span>
      <button
        className="feedback-btn"
        title="Yes, helpful"
        onClick={() => onFeedback(msgIndex, true)}
      >
        👍
      </button>
      <button
        className="feedback-btn"
        title="No, not helpful"
        onClick={() => onFeedback(msgIndex, false)}
      >
        👎
      </button>
    </div>
  )
}