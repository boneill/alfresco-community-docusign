import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeedimDocusignComponent } from './seedim-docusign.component';

describe('SeedimDocusignComponent', () => {
  let component: SeedimDocusignComponent;
  let fixture: ComponentFixture<SeedimDocusignComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SeedimDocusignComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SeedimDocusignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
