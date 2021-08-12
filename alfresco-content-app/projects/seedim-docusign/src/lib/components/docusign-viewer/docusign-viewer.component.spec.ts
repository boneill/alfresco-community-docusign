import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocusignViewerComponent } from './docusign-viewer.component';

describe('DocusignViewerComponent', () => {
  let component: DocusignViewerComponent;
  let fixture: ComponentFixture<DocusignViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DocusignViewerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DocusignViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
