import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  console.log('🔵 [JWTInterceptor] URL:', req.url);
  console.log('🔵 [JWTInterceptor] Token présent:', !!token);

  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    console.log('🔵 [JWTInterceptor] ✅ Token ajouté');
    return next(cloned);
  }

  console.log('🔵 [JWTInterceptor] ❌ Pas de token');
  return next(req);
};