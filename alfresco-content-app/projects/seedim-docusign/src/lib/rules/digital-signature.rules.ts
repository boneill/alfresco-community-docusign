import { RuleContext } from '@alfresco/adf-extensions';

/**
 * Checks if user has selected a file.
 * JSON ref: `app.selection.file`
 */
export function hasFileSelected(context: RuleContext): boolean {
  if (context && context.selection && context.selection.file) {
    return true;
  }
  return false;
}
export function isSupportedMimeType(context: RuleContext): boolean {
  //console.log('isSupportedMimetype');

  var isSupported = false;

  if (context.selection.file.entry && context.selection.file.entry.content) {
    const mimeType: string = context.selection.file.entry.content.mimeType;
    //console.log('Rule isSupportedMimetype: mimetype: ', mimeType);
    switch (mimeType.toLowerCase()) {
      case 'application/pdf':
        isSupported = true;
        break;
      case 'image/png':
        isSupported = true;
        break;
      case 'image/jpeg':
        isSupported = true;
        break;
      case 'image/gif':
        isSupported = true;
        break;
      case 'image/bmp':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
        isSupported = true;
        break;
      case 'application/msword':
        isSupported = true;
        break;
      case 'application/vnd.ms-word.document.macroenabled.12':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.wordprocessingml.template':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.presentationml.presentation':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.presentationml.template':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.presentationml.slideshow':
        isSupported = true;
        break;
      case 'application/vnd.ms-powerpoint':
        isSupported = true;
        break;
      case 'application/vnd.ms-powerpoint.presentation.macroenabled.12':
        isSupported = true;
        break;
      case 'application/vnd.ms-excel':
        isSupported = true;
        break;
      case 'application/vnd.ms-excel.sheet.macroenabled.12':
        isSupported = true;
        break;
      case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
        isSupported = true;
        break;
      case 'image/tiff':
        isSupported = true;
        break;
      case 'text/html':
        isSupported = true;
        break;
      case 'text/csv':
        isSupported = true;
        break;
      case 'application/vnd.ms-outlook':
        isSupported = true;
        break;
      case 'application/rtf':
        isSupported = true;
        break;
      case 'text/plain':
        isSupported = true;
        break;
      case 'application/wordperfect':
        isSupported = true;
        break;
      default:
        isSupported = false;
    }
  }

  //console.log('Rule isSupportedMimetype: return: ', isSupported);

  return isSupported;
}

export function isSigned(context: RuleContext): boolean {
  if (hasFileSelected(context)) {
    if (
      context.selection.file.entry.properties &&
      context.selection.file.entry.properties['docusign:documentType'] ===
        'Original' &&
      context.selection.file.entry.properties['docusign:status'] === 'completed'
    ) {
      return true;
    }
  }

  return false;
}

/**
 * Checks if document can be digitally signed.
 * JSON ref: `canBeSigned`
 * @param context Rule execution context
 */
export function canBeSigned(context: RuleContext): boolean {
  if (hasFileSelected(context)) {
    if (
      context.selection.file.entry.aspectNames &&
      !(
        context.selection.file.entry.aspectNames.includes(
          'docusign:digitalSignature'
        ) ||
        context.selection.file.entry.aspectNames.includes(
          'docusign:signatureResponseDocumentAspect'
        )
      ) &&
      isSupportedMimeType(context)
    ) {
      return true;
    }
  }
  return false;
}

/**
 * Checks if document is the signed document.
 * JSON ref: `isSignedDocument`
 * @param context Rule execution context
 */
export function isSignedDocument(context: RuleContext): boolean {
  console.log('Rule isSuppoisrtedMimetype: return: ', context);

  if (hasFileSelected(context)) {
    if (
      context.selection.file.entry.aspectNames &&
      context.selection.file.entry.aspectNames.includes(
        'docusign:signedDocumentAspect'
      )
    ) {
      console.log('isSignedDocument true');
      return true;
    }
  }
  console.log('isSignedDocument false');
  return false;
}
