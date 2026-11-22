import { useEffect, useRef, useState } from 'react';
import { GroupChannel, MessageHandler, NCEngine, SendTextMessageParams } from '@nexconn/chat';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

export default function ChatRoom() {
  const [config, setConfig] = useState(null);
  const [status, setStatus] = useState('Loading chat configuration');
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const channelRef = useRef(null);
  const seenRef = useRef(new Set());

  useEffect(() => {
    let active = true;
    async function connect() {
      try {
        const response = await fetch('/api/v1/chat/config');
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Nexconn is not configured.');
        if (!active) return;
        setConfig(data);
        NCEngine.initialize({ appKey: data.appKey });
        NCEngine.addMessageHandler('telyvo-message-handler', new MessageHandler({
          onMessageReceived({ messages: incoming }) {
            if (!active) return;
            setMessages((current) => {
              const next = [...current];
              incoming.forEach((message) => {
                const id = message.messageId || `${message.senderUserId}-${message.sentTime}`;
                if (seenRef.current.has(id)) return;
                seenRef.current.add(id);
                const content = message.content?.text || message.content?.content || message.content || '';
                next.push({ id, sender: message.senderUserId || 'Team member', text: typeof content === 'string' ? content : JSON.stringify(content), sentTime: message.sentTime || Date.now() });
              });
              return next.slice(-100);
            });
          }
        }));
        const result = await NCEngine.connect({ token: data.token });
        if (!active) return;
        if (!result.isOk) throw new Error(`Nexconn connection failed (code ${result.code}).`);
        channelRef.current = new GroupChannel(data.groupId);
        setStatus(`Connected as ${data.userId}`);
      } catch (error) {
        if (active) setStatus(error.message || 'Unable to connect to Nexconn.');
      }
    }
    connect();
    return () => { active = false; };
  }, []);

  async function sendMessage(event) {
    event.preventDefault();
    const value = text.trim();
    if (!value || !channelRef.current || sending) return;
    setSending(true);
    try {
      const result = await channelRef.current.sendMessage(new SendTextMessageParams({ text: value }));
      if (result.code !== 0) throw new Error(`Message send failed (code ${result.code}).`);
      const message = result.data;
      const id = message?.messageId || `local-${Date.now()}`;
      if (!seenRef.current.has(id)) {
        seenRef.current.add(id);
        setMessages((current) => [...current, { id, sender: config.userId, text: value, sentTime: message?.sentTime || Date.now() }].slice(-100));
      }
      setText('');
    } catch (error) {
      setStatus(error.message || 'Unable to send message.');
    } finally { setSending(false); }
  }

  return (
    <section className="chat-section" id="discuss">
      <div className="section-heading"><div><span className="section-kicker">TEAM COLLABORATION · NEXCONN</span><h2>Discuss the evidence.</h2></div><span className="section-note">{status}</span></div>
      <div className="chat-panel">
        <div className="chat-feed" aria-live="polite">
          {messages.length === 0 && <p className="chat-empty">No messages yet. Share the decision context with your team.</p>}
          {messages.map((message) => <article className="chat-message" key={message.id}><span>{message.sender}</span><p>{message.text}</p><time>{new Date(message.sentTime).toLocaleTimeString()}</time></article>)}
        </div>
        <form className="chat-compose" onSubmit={sendMessage}>
          <input value={text} onChange={(event) => setText(event.target.value)} maxLength={1000} placeholder="Discuss a finding or ask your team…" aria-label="Team message" disabled={!channelRef.current} />
          <button type="submit" disabled={!text.trim() || sending || !channelRef.current}><SendRoundedIcon /> Send</button>
        </form>
        <p className="chat-hint">Messages are delivered through the Nexconn Chat SDK. The configured group must exist and include this user.</p>
      </div>
    </section>
  );
}