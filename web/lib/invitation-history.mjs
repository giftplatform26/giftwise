/** One immutable state transition per edit. Safe when React replays reducers in strict mode. */
const same=(a,b)=>a===b||JSON.stringify(a)===JSON.stringify(b);
const push=(past,value)=>[...past.slice(-49),value];
export function invitationHistoryReducer(state,action){
 switch(action.type){
  case 'preview':{const next=typeof action.next==='function'?action.next(state.present):action.next;return same(next,state.present)?state:{...state,present:next};}
  case 'commit':{const next=typeof action.next==='function'?action.next(state.present):action.next;return same(next,state.present)?state:{present:next,past:push(state.past,state.present),future:[]};}
  case 'finish':return same(action.before,state.present)?state:{...state,past:push(state.past,action.before),future:[]};
  case 'undo':return state.past.length?{present:state.past.at(-1),past:state.past.slice(0,-1),future:[state.present,...state.future]}:state;
  case 'redo':return state.future.length?{present:state.future[0],past:push(state.past,state.present),future:state.future.slice(1)}:state;
  case 'clear':return {...state,past:[],future:[]};
  default:return state;
 }
}
