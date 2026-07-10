/** Reflète la sérialisation JSON d'org.springframework.data.domain.Page. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;   // page courante (0-indexée)
  size: number;
  first: boolean;
  last: boolean;
}
