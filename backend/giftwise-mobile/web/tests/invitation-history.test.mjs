import test from 'node:test';
import assert from 'node:assert/strict';
import {invitationHistoryReducer as reduce} from '../lib/invitation-history.mjs';
import {initialDesign,updateElement} from '../lib/design.mjs';
const base=()=>({present:structuredClone(initialDesign),past:[],future:[]});
const text=state=>state.present.elements.find(e=>e.id==='title').text;
test('text edits undo and redo the actual design and clear redo after a new edit',()=>{
 let state=base();const original=text(state);state=reduce(state,{type:'commit',next:updateElement(state.present,'title',{text:'Our wedding'})});
 assert.equal(text(state),'Our wedding');state=reduce(state,{type:'undo'});assert.equal(text(state),original);state=reduce(state,{type:'redo'});assert.equal(text(state),'Our wedding');
 state=reduce(state,{type:'undo'});state=reduce(state,{type:'commit',next:updateElement(state.present,'title',{text:'Birthday'})});assert.equal(state.future.length,0);assert.equal(text(reduce(state,{type:'redo'})),'Birthday');
});
test('many drag previews form one undo step and replaying the reducer cannot double-add history',()=>{
 const start=base();let state=start;for(let x=51;x<=100;x++)state=reduce(state,{type:'preview',next:current=>updateElement(current,'title',{x})});assert.equal(state.past.length,0);
 const action={type:'finish',before:start.present};const finished=reduce(state,action);assert.deepEqual(reduce(state,action),finished);assert.equal(finished.past.length,1);assert.equal(state.past.length,0);
 const undone=reduce(finished,{type:'undo'});assert.equal(undone.present.elements[0].x,50);assert.equal(reduce(undone,{type:'redo'}).present.elements[0].x,100);
});
test('no-op gestures do not add undo steps and history has a bounded size',()=>{
 let state=base();assert.equal(reduce(state,{type:'finish',before:state.present}),state);for(let i=0;i<70;i++)state=reduce(state,{type:'commit',next:updateElement(state.present,'title',{text:String(i)})});assert.equal(state.past.length,50);
});
