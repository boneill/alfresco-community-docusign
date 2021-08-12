import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SupplementalDocumentsComponent } from './supplemental-documents.component';

describe('SupplementalDocumentsComponent', () => {
  let component: SupplementalDocumentsComponent;
  let fixture: ComponentFixture<SupplementalDocumentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SupplementalDocumentsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SupplementalDocumentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
