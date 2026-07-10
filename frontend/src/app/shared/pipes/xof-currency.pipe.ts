import { Pipe, PipeTransform } from '@angular/core';

/** Formate un montant avec sa devise, ex: 15000 'XOF' -> "15 000 XOF". */
@Pipe({
  name: 'ppCurrency',
  standalone: true
})
export class XofCurrencyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency: string = 'XOF'): string {
    if (amount === null || amount === undefined) return '';
    const formatted = new Intl.NumberFormat('fr-FR').format(amount);
    return `${formatted} ${currency}`;
  }
}
