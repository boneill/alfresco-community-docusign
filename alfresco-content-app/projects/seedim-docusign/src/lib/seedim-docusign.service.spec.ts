import { TestBed } from '@angular/core/testing';

import { SeedimDocusignService } from './seedim-docusign.service';

describe('SeedimDocusignService', () => {
  let service: SeedimDocusignService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SeedimDocusignService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
