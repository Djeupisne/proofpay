import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { adminGuard } from './core/auth/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'transactions',
    canActivate: [authGuard],
    loadComponent: () => import('./features/transactions/list/transaction-list.component').then(m => m.TransactionListComponent)
  },
  {
    path: 'transactions/new',
    canActivate: [authGuard],
    loadComponent: () => import('./features/transactions/create/transaction-create.component').then(m => m.TransactionCreateComponent)
  },
  {
    path: 'transactions/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/transactions/detail/transaction-detail.component').then(m => m.TransactionDetailComponent)
  },
{
  path: 'transactions/:id/success',
  canActivate: [authGuard],
  loadComponent: () => import('./features/transactions/success/transaction-success.component').then(m => m.TransactionSuccessComponent)
},
  {
    path: 'transactions/:id/dispute',
    canActivate: [authGuard],
    loadComponent: () => import('./features/disputes/dispute-form.component').then(m => m.DisputeFormComponent)
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/search/admin-search.component').then(m => m.AdminSearchComponent)
  },
  {
    path: 'admin/disputes',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/dispute-decision/dispute-decision.component').then(m => m.DisputeDecisionComponent)
  },
  {
    path: 'admin/settings',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/settings/admin-settings.component').then(m => m.AdminSettingsComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  { path: '', pathMatch: 'full', redirectTo: 'transactions' },
  { path: '**', redirectTo: 'transactions' }
];
