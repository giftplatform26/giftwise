import test from 'node:test';
import assert from 'node:assert/strict';
import React from 'react';
import {renderToStaticMarkup} from 'react-dom/server';
import {prepareCatalog,localize,localizeText,activeLocale} from '../lib/localization.mjs';
function catalog(selected='en',resources={},sources={},defaultLanguage='en',fallbackLanguage='en'){return {selectedLanguage:selected,defaultLanguage,fallbackLanguage,resources:{en:{},...resources},sources};}
test('localized UI preserves named values and escapes markup at the React boundary',async()=>{await prepareCatalog(catalog('ml',{ml:{'ui.name':'നമസ്കാരം {{name}}'}},{'ui.name':'Hello {{name}}'}));const text=localize('ui.name',{name:'<script>Alice</script>'});assert.equal(text,'നമസ്കാരം <script>Alice</script>');const html=renderToStaticMarkup(React.createElement('span',null,text));assert.match(html,/&lt;script&gt;/);assert.doesNotMatch(html,/<script>/)});
test('missing requested translation falls back to platform default before source',async()=>{await prepareCatalog(catalog('kn',{ml:{'ui.save':'സേവ് ചെയ്യുക'}},{'ui.save':'Save'},'ml'));assert.equal(localize('ui.save'),'സേവ് ചെയ്യുക');await prepareCatalog(catalog('kn',{}, {'ui.save':'Save'},'ml'));assert.equal(localize('ui.save'),'Save')});
test('unpublishing clears cached translations rather than keeping old content',async()=>{await prepareCatalog(catalog('te',{te:{'ui.save':'సేవ్ చేయండి'}},{'ui.save':'Save'}));assert.equal(localize('ui.save'),'సేవ్ చేయండి');await prepareCatalog(catalog('te',{te:{}},{'ui.save':'Save'}));assert.equal(localize('ui.save'),'Save')});
test('rendered feedback templates retain original variable values',async()=>{await prepareCatalog(catalog('hi',{hi:{'ui.feedback':'{{name}} के लिए {{amount}}'}},{'ui.feedback':'{{amount}} for {{name}}'}));assert.equal(localizeText('₹100.00 for Alice'),'Alice के लिए ₹100.00')});
test('unregistered customer content is returned unchanged',async()=>{await prepareCatalog(catalog('ta',{},{}));assert.equal(localizeText('My private family message'),'My private family message')});
test('existing English formatter locale is preserved and future locales are data driven',async()=>{await prepareCatalog(catalog());assert.equal(activeLocale('en-US'),'en-US');assert.equal(activeLocale(),'en-IN');await prepareCatalog(catalog('ar'));assert.equal(activeLocale('en-IN'),'ar')});
