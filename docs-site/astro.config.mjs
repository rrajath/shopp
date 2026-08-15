// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

import cloudflare from '@astrojs/cloudflare';

// https://astro.build/config
export default defineConfig({
  integrations: [
      starlight({
          title: 'Shopp',
          description: 'A fast, native Android shopping list app. Documentation for the current build.',
          social: [
              { icon: 'github', label: 'GitHub', href: 'https://github.com/rrajath/shopp' },
              { icon: 'download', label: 'Releases', href: 'https://github.com/rrajath/shopp/releases/latest' },
          ],
          customCss: ['./src/styles/custom.css'],
          editLink: { baseUrl: 'https://github.com/rrajath/shopp/edit/main/docs-site/' },
          sidebar: [
              { label: '← Back to the launch page', link: '/' },
              {
                  label: 'Documentation',
                  items: [
                      { label: 'Overview', slug: 'docs' },
                      { label: 'The list screen', slug: 'docs/list' },
                      { label: 'Quick Add', slug: 'docs/quick-add' },
                      { label: 'Labels by typing', slug: 'docs/labels' },
                      { label: 'Recently Completed', slug: 'docs/recently-completed' },
                      { label: 'Settings', slug: 'docs/settings' },
                      { label: 'Merging a mistyped label', slug: 'docs/merging-labels' },
                      { label: 'Capture from outside the app', slug: 'docs/capture' },
                  ],
              },
              {
                  label: 'Appendix',
                  items: [
                      { label: 'Data model', slug: 'docs/data-model' },
                      { label: 'Cut from v1, and why', slug: 'docs/cut-from-v1' },
                  ],
              },
          ],
      }),
	],

  adapter: cloudflare(),
});
