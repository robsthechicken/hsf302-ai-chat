(() => {
  const input = document.getElementById('personaContent');
  const count = document.getElementById('personaCount');
  if (!input || !count) return;

  const validate = () => {
    // Match the server's trim and UTF-16 length without changing the draft.
    const length = input.value.replace(/^[\x00-\x20]+|[\x00-\x20]+$/g, '').length;
    const message = length > 2000
      ? `Nội dung có ${length} ký tự, vượt giới hạn 2000 ký tự. Vui lòng rút gọn trước khi lưu.`
      : length === 0 ? 'Vui lòng nhập nội dung giọng điệu trợ lý.' : '';
    input.setCustomValidity(message);
    input.setAttribute('aria-invalid', String(Boolean(message)));
    count.textContent = message || `${length}/2000 ký tự`;
    count.classList.toggle('text-danger', Boolean(message));
  };
  input.addEventListener('input', validate);
  validate();
})();
