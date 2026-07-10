import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Ajoute une clé d'idempotence sur les requêtes de paiement/confirmation
 * (règle métier #16 : les paiements doivent être idempotents).
 */
export const idempotencyInterceptor: HttpInterceptorFn = (req, next) => {
  const sensitivePaths = ['/pay', '/confirm', '/mark-delivered'];
  if (sensitivePaths.some(path => req.url.includes(path)) && req.method === 'POST') {
    const key = crypto.randomUUID();
    req = req.clone({ setHeaders: { 'Idempotency-Key': key } });
  }
  return next(req);
};
