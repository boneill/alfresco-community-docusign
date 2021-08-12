import { Action } from '@ngrx/store';

export const SIGN_DOCUMENT = 'SIGN_DOCUMENT';
export const SIGN_DOCUMENT_FAIL = 'SIGN_DOCUMENT_FAIL';
export const NAVIGATE_TO_SIGNED_DOCUMENT = 'NAVIGATE_TO_SIGNED_DOCUMENT';
export const NAVIGATE_TO_SIGNED_DOCUMENT_FAIL =
  'NAVIGATE_TO_SIGNED_DOCUMENT_FAIL';
export const NAVIGATE_TO_CERTIFIED_DOCUMENT = 'NAVIGATE_TO_CERTIFIED_DOCUMENT';
export const NAVIGATE_TO_CERTIFIED_DOCUMENT_FAIL =
  'NAVIGATE_TO_CERTIFIED_DOCUMENT_FAIL';

//action creators

export class SignDocument implements Action {
  readonly type = SIGN_DOCUMENT;
  constructor(public payload: any) {}
}
export class SignDocumentFail implements Action {
  readonly type = SIGN_DOCUMENT_FAIL;
  constructor(public payload: any) {}
}
export class NavigateToSignedDocument implements Action {
  readonly type = NAVIGATE_TO_SIGNED_DOCUMENT;
  constructor(public payload: any) {}
}
export class NavigateToSignedDocumentFail implements Action {
  readonly type = NAVIGATE_TO_SIGNED_DOCUMENT_FAIL;
  constructor(public payload: any) {}
}

export class NavigateToCertifiedDocument implements Action {
  readonly type = NAVIGATE_TO_CERTIFIED_DOCUMENT;
  constructor(public payload: any) {}
}
export class NavigateToCertifiedDocumentFail implements Action {
  readonly type = NAVIGATE_TO_CERTIFIED_DOCUMENT_FAIL;
  constructor(public payload: any) {}
}

export type DigitalSignatureAction =
  | SignDocument
  | SignDocumentFail
  | NavigateToSignedDocument
  | NavigateToSignedDocumentFail
  | NavigateToCertifiedDocument
  | NavigateToCertifiedDocumentFail;
