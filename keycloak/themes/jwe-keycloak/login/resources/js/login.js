const BASE64URL_ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_';
const REENCRYPT_INTERVAL_MS = 3600;
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

function randomBase64Url(length) {
    const bytes = crypto.getRandomValues(new Uint8Array(length));
    return Array.from(bytes, byte => BASE64URL_ALPHABET[byte & 63]).join('');
}

function scrambleTo(element, target, durationMs) {
    return new Promise(resolve => {
        const start = performance.now();
        const frame = now => {
            const progress = Math.min((now - start) / durationMs, 1);
            const settled = Math.floor(target.length * progress);
            element.textContent = target.slice(0, settled) + randomBase64Url(target.length - settled);
            if (progress < 1) {
                requestAnimationFrame(frame);
            } else {
                resolve();
            }
        };
        requestAnimationFrame(frame);
    });
}

function animateToken(token) {
    const parts = Array.from(token.querySelectorAll('[data-cipher-length]'));
    const reencrypt = async () => {
        if (!document.hidden) {
            await Promise.all(parts.map((part, index) =>
                wait(index * 140).then(() =>
                    scrambleTo(part, randomBase64Url(Number(part.dataset.cipherLength)), 900))));
        }
        setTimeout(reencrypt, REENCRYPT_INTERVAL_MS);
    };
    setTimeout(reencrypt, REENCRYPT_INTERVAL_MS / 2);
}

function setupPasswordToggle(button) {
    const input = document.getElementById(button.getAttribute('aria-controls'));
    button.addEventListener('click', () => {
        const reveal = input.type === 'password';
        input.type = reveal ? 'text' : 'password';
        button.textContent = reveal ? button.dataset.labelHide : button.dataset.labelShow;
    });
}

function setupCapsLockHint(input, hint) {
    const update = event => {
        hint.hidden = !event.getModifierState('CapsLock');
    };
    input.addEventListener('keydown', update);
    input.addEventListener('keyup', update);
    input.addEventListener('blur', () => {
        hint.hidden = true;
    });
}

function setupSubmitState(form) {
    const button = form.querySelector('[data-submit]');
    const label = button.querySelector('[data-submit-label]');
    const idleLabel = label.textContent;

    form.addEventListener('submit', () => {
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
        if (prefersReducedMotion.matches) {
            label.textContent = button.dataset.labelBusy;
        } else {
            scrambleTo(label, button.dataset.labelBusy, 450);
        }
    });

    // The back/forward cache restores this page with the button still disabled from the last submit.
    window.addEventListener('pageshow', event => {
        if (!event.persisted) {
            return;
        }
        button.disabled = false;
        button.removeAttribute('aria-busy');
        label.textContent = idleLabel;
    });
}

const token = document.querySelector('[data-jwe-token]');
if (token && !prefersReducedMotion.matches) {
    animateToken(token);
}

const passwordToggle = document.querySelector('[data-password-toggle]');
if (passwordToggle) {
    setupPasswordToggle(passwordToggle);
}

const password = document.getElementById('password');
const capsLockHint = document.querySelector('[data-caps-lock-hint]');
if (password && capsLockHint) {
    setupCapsLockHint(password, capsLockHint);
}

const loginForm = document.querySelector('[data-login-form]');
if (loginForm) {
    setupSubmitState(loginForm);
}
