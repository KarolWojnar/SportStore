import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  standalone: true,
  name: 'highlight'
})
export class HighlightPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(text: string, search: string): SafeHtml {
    if (!search) return text;
    const regex = new RegExp(`(${search})`, 'gi');
    const highlighted = text.replace(regex, '<span class="text-success text-bg-success bg-opacity-10 rounded">$1</span>');
    return this.sanitizer.bypassSecurityTrustHtml(highlighted);
  }
}
