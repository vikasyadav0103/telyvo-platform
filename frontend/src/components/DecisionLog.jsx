import { useEffect, useMemo, useState } from 'react';
import SaveRoundedIcon from '@mui/icons-material/SaveRounded';
import DownloadRoundedIcon from '@mui/icons-material/DownloadRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import FactCheckRoundedIcon from '@mui/icons-material/FactCheckRounded';

const STORAGE_KEY = 'telyvo.decision-records.v1';
const STATUSES = ['Proposed', 'Accepted', 'Rejected', 'Needs more research'];

function readRecords() {
  try {
    const value = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    return Array.isArray(value) ? value : [];
  } catch {
    return [];
  }
}

function safeFileName(value) {
  return (value || 'technology-decision').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 60) || 'technology-decision';
}

function toMarkdown(record) {
  const sources = (record.sources || []).map((source, index) => `${index + 1}. [${source.title || 'Source'}](${source.url})`).join('\n');
  return [
    `# ${record.title}`,
    '',
    `- **Status:** ${record.status}`,
    `- **Created:** ${new Date(record.createdAt).toLocaleString()}`,
    '',
    '## Decision question',
    record.query,
    '',
    '## Team decision and rationale',
    record.rationale || '_No team rationale added._',
    '',
    '## Research evidence',
    record.answer || '_No research answer saved._',
    '',
    '## Sources',
    sources || '_No sources returned._',
    ''
  ].join('\n');
}

export default function DecisionLog({ query, research }) {
  const [records, setRecords] = useState(readRecords);
  const [title, setTitle] = useState('');
  const [status, setStatus] = useState('Proposed');
  const [rationale, setRationale] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
    } catch {
      setNotice('Browser storage is full or unavailable. Export existing records before continuing.');
    }
  }, [records]);

  const canSave = Boolean(query?.trim() && research?.answer?.trim());
  const sortedRecords = useMemo(
    () => [...records].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)),
    [records]
  );

  function saveDecision(event) {
    event.preventDefault();
    if (!canSave) return;
    const record = {
      id: globalThis.crypto?.randomUUID?.() || `decision-${Date.now()}`,
      title: title.trim() || query.trim().slice(0, 90),
      query: query.trim(),
      status,
      rationale: rationale.trim(),
      answer: research.answer,
      sources: (research.sources || []).map(({ title: sourceTitle, url }) => ({ title: sourceTitle, url })),
      createdAt: new Date().toISOString()
    };
    setRecords((current) => [record, ...current].slice(0, 50));
    setTitle('');
    setRationale('');
    setStatus('Proposed');
    setNotice('Decision record saved in this browser.');
  }

  function exportRecord(record) {
    const blob = new Blob([toMarkdown(record)], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${safeFileName(record.title)}.md`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
  }

  function removeRecord(id) {
    setRecords((current) => current.filter((record) => record.id !== id));
    setNotice('Decision record deleted from this browser.');
  }

  function updateStatus(id, nextStatus) {
    setRecords((current) => current.map((record) => record.id === id ? { ...record, status: nextStatus, updatedAt: new Date().toISOString() } : record));
  }

  return (
    <section className="decision-log" aria-labelledby="decision-log-title">
      <div className="section-heading">
        <div><span className="section-kicker">DECISION GOVERNANCE</span><h2 id="decision-log-title">Save the decision.</h2></div>
        <span className="section-note">{records.length}/50 local records</span>
      </div>
      <form className="decision-form" onSubmit={saveDecision}>
        <div className="decision-form-heading"><FactCheckRoundedIcon /><div><strong>Create a decision record</strong><span>Capture the question, evidence and the team's final rationale.</span></div></div>
        <label>Decision title<input value={title} onChange={(event) => setTitle(event.target.value)} maxLength={120} placeholder={query?.trim() || 'e.g. Select a backend framework'} /></label>
        <div className="decision-form-row">
          <label>Decision status<select value={status} onChange={(event) => setStatus(event.target.value)}>{STATUSES.map((item) => <option key={item}>{item}</option>)}</select></label>
          <label>Team rationale<textarea value={rationale} onChange={(event) => setRationale(event.target.value)} maxLength={3000} rows={3} placeholder="Why did the team choose this option? Note constraints, risks and follow-up actions." /></label>
        </div>
        <div className="decision-form-footer"><span>{canSave ? 'Research answer and source list will be included.' : 'Run You.com research before saving a decision.'}</span><button type="submit" disabled={!canSave}><SaveRoundedIcon /> Save decision</button></div>
        {notice && <p className="decision-notice" role="status">{notice}</p>}
      </form>

      <div className="saved-decisions">
        <h3>Saved decision history</h3>
        {sortedRecords.length === 0 && <p className="decision-empty">No decisions saved yet. Research a technology question and save the resulting evidence here.</p>}
        {sortedRecords.map((record) => (
          <article className="saved-decision" key={record.id}>
            <div className="saved-decision-main"><span className="saved-decision-date">{new Date(record.createdAt).toLocaleString()}</span><h4>{record.title}</h4><p>{record.query}</p><span className={`decision-status status-${record.status.toLowerCase().replace(/[^a-z]+/g, '-')}`}>{record.status}</span>{record.rationale && <p className="saved-rationale">{record.rationale}</p>}</div>
            <div className="saved-decision-actions"><select aria-label={`Update status for ${record.title}`} value={record.status} onChange={(event) => updateStatus(record.id, event.target.value)}>{STATUSES.map((item) => <option key={item}>{item}</option>)}</select><button type="button" onClick={() => exportRecord(record)} aria-label={`Export ${record.title}`}><DownloadRoundedIcon /> Export</button><button type="button" className="delete-decision" onClick={() => removeRecord(record.id)} aria-label={`Delete ${record.title}`}><DeleteOutlineRoundedIcon /></button></div>
          </article>
        ))}
      </div>
      <p className="decision-storage-note">Records are stored in this browser only (localStorage). Export Markdown to share or archive them; server-side team persistence is not enabled in this MVP.</p>
    </section>
  );
}
