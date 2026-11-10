// Styled, translatable replacement for window.confirm / window.alert.
// Usage: if (!await confirmAction('Remove this gift?', {confirmLabel: 'Remove', danger: true})) return;
import {localizeText} from './localization.mjs';

const STYLE_ID = 'gw-confirm-style';
const CSS = `.gwConfirmBackdrop{position:fixed;inset:0;background:#2a1f2a66;display:flex;align-items:center;justify-content:center;padding:16px;z-index:10000}
.gwConfirm{background:#fffefa;color:#35292a;border-radius:18px;max-width:440px;width:100%;padding:24px;box-shadow:0 24px 60px #2a1f2a40;font:inherit}
.gwConfirm p{margin:0 0 20px;line-height:1.55;white-space:pre-line}
.gwConfirmActions{display:flex;gap:10px;justify-content:flex-end;flex-wrap:wrap}
.gwConfirmActions button{min-height:44px;padding:10px 18px;border-radius:12px;border:1px solid #dacac1;background:#fff;color:#35292a;font-weight:650;cursor:pointer}
.gwConfirmActions .gwConfirmPrimary{background:#6336b5;border-color:#6336b5;color:#fff}
.gwConfirmActions .gwConfirmDanger{background:#9c3028;border-color:#9c3028;color:#fff}`;

function ensureStyle() {
  if (document.getElementById(STYLE_ID)) return;
  const style = document.createElement('style'); style.id = STYLE_ID; style.textContent = CSS; document.head.appendChild(style);
}

function open(message, {confirmLabel = 'Continue', cancelLabel = 'Cancel', danger = false, single = false} = {}) {
  if (typeof document === 'undefined') return Promise.resolve(false);
  ensureStyle();
  return new Promise(resolve => {
    const previous = document.activeElement;
    const backdrop = document.createElement('div'); backdrop.className = 'gwConfirmBackdrop';
    const dialog = document.createElement('div'); dialog.className = 'gwConfirm';
    dialog.setAttribute('role', single ? 'dialog' : 'alertdialog'); dialog.setAttribute('aria-modal', 'true');
    const text = document.createElement('p'); text.id = 'gw-confirm-' + Date.now(); text.textContent = localizeText(message);
    dialog.setAttribute('aria-describedby', text.id);
    const actions = document.createElement('div'); actions.className = 'gwConfirmActions';
    const ok = document.createElement('button'); ok.type = 'button'; ok.textContent = localizeText(confirmLabel);
    ok.className = danger ? 'gwConfirmDanger' : 'gwConfirmPrimary';
    const cancel = document.createElement('button'); cancel.type = 'button'; cancel.textContent = localizeText(cancelLabel);
    if (!single) actions.appendChild(cancel);
    actions.appendChild(ok); dialog.append(text, actions); backdrop.appendChild(dialog); document.body.appendChild(backdrop);
    const finish = value => { document.removeEventListener('keydown', onKey, true); backdrop.remove(); if (previous && previous.focus) previous.focus(); resolve(value); };
    const onKey = event => {
      if (event.key === 'Escape') { event.preventDefault(); finish(single); }
      if (event.key === 'Tab') { const items = single ? [ok] : [cancel, ok]; const i = items.indexOf(document.activeElement); event.preventDefault(); items[(i + (event.shiftKey ? items.length - 1 : 1)) % items.length].focus(); }
    };
    ok.addEventListener('click', () => finish(true));
    cancel.addEventListener('click', () => finish(false));
    backdrop.addEventListener('click', event => { if (event.target === backdrop) finish(single); });
    document.addEventListener('keydown', onKey, true);
    (single || !danger ? ok : cancel).focus();
  });
}

/** Ask the user to confirm. Resolves true only when they choose the confirm action. */
export function confirmAction(message, options) { return open(message, options); }
/** Show a message with a single OK button. */
export function notify(message, {okLabel = 'OK'} = {}) { return open(message, {confirmLabel: okLabel, single: true}).then(() => undefined); }
