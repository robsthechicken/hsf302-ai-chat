(() => {
  'use strict';
  const panel = document.querySelector('[data-chat-endpoint]');
  const form = document.getElementById('chatForm');
  const input = document.getElementById('chatInput');
  const messages = document.getElementById('chatMessages');
  const send = document.getElementById('sendChat');
  if (!panel || !form || !input || !messages || !send) return;
  let busy = false;
  const endpoint = panel.dataset.chatEndpoint;
  function bubble(text, kind, sources = []) {
    const article = document.createElement('div');
    article.className = 'bubble ' + kind;
    const content = document.createElement('div');
    content.className = 'bubble-text';
    content.textContent = text;
    article.appendChild(content);
    if (Array.isArray(sources) && sources.length) {
      const list = document.createElement('div');
      list.className = 'source-list';
      const caption = document.createElement('strong');
      caption.textContent = 'Nguồn tham khảo';
      list.appendChild(caption);
      sources.forEach(s => {
        const item = document.createElement('div');
        item.className = 'source-item';
        const name = document.createElement('strong');
        name.textContent = s.source || 'Nguồn';
        const score = document.createElement('span');
        score.textContent = typeof s.score === 'number' ? ' · ' + s.score.toFixed(2) : '';
        const preview = document.createElement('p');
        preview.textContent = s.preview || '';
        item.appendChild(name);
        item.appendChild(score);
        item.appendChild(preview);
        list.appendChild(item);
      });
      article.appendChild(list);
    }
    messages.appendChild(article);
    messages.scrollTop = messages.scrollHeight;
    return article;
  }
  input.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); form.requestSubmit(); }
  });
  form.addEventListener('submit', async e => {
    e.preventDefault();
    const value = input.value.trim();
    if (!value || busy) return;
    busy = true;
    send.disabled = true;
    send.textContent = 'Đang trả lời...';
    bubble(value, 'human');
    input.value = '';
    const pending = bubble('Đang tìm câu trả lời...', 'bot pending');
    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ message: value })
      });
      const body = await response.json();
      pending.remove();
      if (!response.ok) {
        bubble(body.error || 'Không thể xử lý yêu cầu.', 'bot error');
        if (response.status === 401) window.location.href = '/login';
      } else bubble(body.answer || 'Chưa có câu trả lời.', 'bot', body.sources);
    } catch (_) {
      pending.remove();
      bubble('Không thể kết nối. Vui lòng thử lại.', 'bot error');
    } finally {
      busy = false;
      send.disabled = false;
      send.textContent = 'Gửi ↗';
      input.focus();
    }
  });
})();
