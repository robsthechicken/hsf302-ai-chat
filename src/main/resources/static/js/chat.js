// Chat dùng chung cho trang khách (/customer/api/chat) và trang chủ quán (/owner/api/chat).
// Câu trả lời của AI được render markdown (marked) rồi lọc qua DOMPurify trước khi gán innerHTML để chặn XSS;
// mọi nội dung khác (câu của người dùng, lỗi, nguồn) gán bằng textContent.
(() => {
  'use strict';
  const panel = document.querySelector('[data-chat-endpoint]');
  const form = document.getElementById('chatForm');
  const input = document.getElementById('chatInput');
  const messages = document.getElementById('chatMessages');
  const send = document.getElementById('sendChat');
  if (!panel || !form || !input || !messages || !send) return;

  const endpoint = panel.dataset.chatEndpoint;
  const sendLabel = send.textContent;
  let busy = false;

  function bubble(text, kind, sources = []) {
    const article = document.createElement('div');
    article.className = 'bubble ' + kind;
    const content = document.createElement('div');
    // Chỉ render markdown cho câu trả lời thật của bot; thiếu thư viện (CDN lỗi) thì hiện văn bản thuần
    if (kind === 'bot' && window.marked && window.DOMPurify) {
      content.className = 'md';
      content.innerHTML = DOMPurify.sanitize(marked.parse(text, { breaks: true }));
    } else {
      content.textContent = text;
    }
    article.appendChild(content);

    if (Array.isArray(sources) && sources.length) {
      const list = document.createElement('div');
      list.className = 'source-list';
      const caption = document.createElement('strong');
      caption.textContent = 'Nguồn';
      list.appendChild(caption);
      sources.forEach(s => {
        const item = document.createElement('div');
        item.className = 'source-item';
        const name = document.createElement('strong');
        name.textContent = s.source || 'Nguồn';
        const score = document.createElement('span');
        score.textContent = typeof s.score === 'number' ? ' · độ khớp ' + s.score.toFixed(2) : '';
        const preview = document.createElement('p');
        preview.textContent = s.preview || '';
        item.append(name, score, preview);
        list.appendChild(item);
      });
      article.appendChild(list);
    }

    messages.appendChild(article);
    messages.scrollTop = messages.scrollHeight;
    return article;
  }

  // Nạp lại lịch sử chat lưu trong session (reload/chuyển trang không mất); chặn gửi tới khi nạp xong để giữ đúng thứ tự
  busy = true;
  fetch(endpoint, { credentials: 'same-origin' })
    .then(r => (r.ok ? r.json() : []))
    .then(entries => entries.forEach(e => bubble(e.text, e.role === 'human' ? 'human' : 'bot', e.sources)))
    .catch(() => {})
    .finally(() => { busy = false; });

  // Enter để gửi, Shift+Enter để xuống dòng
  input.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      form.requestSubmit();
    }
  });

  // Nút có data-question ("Câu hỏi mẫu" của chủ quán, "Hỏi về món này" trên thẻ món):
  // cuộn tới khung chat nếu nút nằm ngoài khung, điền câu hỏi rồi gửi luôn
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  document.querySelectorAll('[data-question]').forEach(button => {
    button.addEventListener('click', () => {
      if (!panel.contains(button)) {
        panel.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' });
      }
      input.value = button.dataset.question;
      form.requestSubmit();
    });
  });

  form.addEventListener('submit', async e => {
    e.preventDefault();
    const value = input.value.trim();
    if (!value || busy) return;

    busy = true;
    send.disabled = true;
    send.textContent = 'Đang gửi…';
    bubble(value, 'human');
    input.value = '';
    const pending = bubble('Đang trả lời…', 'bot pending');

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
      } else {
        bubble(body.answer || 'Chưa có câu trả lời.', 'bot', body.sources);
      }
    } catch (_) {
      pending.remove();
      bubble('Không kết nối được tới máy chủ. Vui lòng thử lại.', 'bot error');
    } finally {
      busy = false;
      send.disabled = false;
      send.textContent = sendLabel;
      input.focus();
    }
  });
})();
