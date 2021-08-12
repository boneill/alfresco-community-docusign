import { Injectable } from '@angular/core';
import { QueryBody } from '@alfresco/js-api';
import { SearchConfigurationInterface } from '@alfresco/adf-core';

@Injectable({
  providedIn: 'root'
})
export class SignatureSearchConfigurationService
  implements SearchConfigurationInterface {
  constructor() {}

  /**
   * Generates a QueryBody object with custom search parameters.
   * @param searchTerm Term text to search for
   * @param maxResults Maximum number of search results to show in a page
   * @param skipCount The offset of the start of the page within the results list
   * @returns Query body defined by the parameters
   */
  public generateQueryBody(
    searchTerm: string,
    maxResults: number,
    skipCount: number
  ): QueryBody {
    const defaultQueryBody: QueryBody = {
      query: {
        query: `${searchTerm} AND docusign:documentType: 'Original' AND ASPECT:'docusign:digitalSignature'`
      },
      include: ['path', 'allowableOperations', 'properties'],
      paging: {
        maxItems: maxResults,
        skipCount: skipCount
      },
      filterQueries: [
        { query: "TYPE:'cm:folder' OR TYPE:'cm:content'" }
        /*,{ query: 'NOT cm:creator:System' }*/
      ],
      sort: [{ type: 'FIELD', field: 'docusign:sentDate', ascending: false }]
    };

    console.log('SearchTerm:', defaultQueryBody.query.query);

    return defaultQueryBody;
  }
}
