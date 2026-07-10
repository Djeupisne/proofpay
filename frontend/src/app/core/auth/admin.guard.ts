import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Vérification MVP côté front (le rôle réel est de toute façon revalidé côté
 * API — cf. SecurityConfig backend : /api/admin/** exige ROLE_ADMIN, attribué
 * via proofpay.admin.bootstrap-phones ou promotion manuelle).
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAdmin()) {
    return true;
  }
  router.navigate(['/transactions']);
  return false;
};
