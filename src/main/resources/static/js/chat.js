// Chat dùng chung cho trang khách (/customer/api/chat) và trang chủ quán (/owner/api/chat).
// Mọi nội dung đều gán bằng textContent (không dùng innerHTML) để chặn XSS từ câu trả lời của AI.
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
    content.textContent = text;
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
