import { Gift, Heart, ShieldCheck, Sparkles, Users } from 'lucide-react';
export default function GiftWiseWelcome() {
  return <section className="gwWelcome" aria-labelledby="giftwise-welcome-title">
    <a className="gwWordmark" href="/login"><img src="/giftwise/symbol.svg" alt="" width="42" height="42"/>GiftWise<span>THOUGHTFULLY TOGETHER</span></a>
    <div className="gwWelcomeCopy"><span className="gwEyebrow"><Sparkles size={16} aria-hidden="true"/> THE PEOPLE. THE MOMENTS. THE JOY.</span><h2 id="giftwise-welcome-title">Life gives us reasons.<br/><em>We bring the celebration.</em></h2><p>One lovely place for your wishes, invitations, shared gifts, and the memories that stay.</p></div>
    <div className="gwScene" aria-hidden="true"><div className="gwSceneOrbit"/><span className="gwSceneSpark gwSceneSparkOne">✦</span><img src="/giftwise/miri.svg" alt="" className="gwMiri"/><div className="gwFloatingNote gwFloatingNoteOne"><span><Gift size={20}/></span><div>A little thought.<small>A meaningful gift.</small></div></div><div className="gwFloatingNote gwFloatingNoteTwo"><span><Heart size={20}/></span><div>Your people, together.<small>That’s the real celebration.</small></div></div><span className="gwSceneSpark gwSceneSparkTwo">✧</span></div>
    <div className="gwWelcomeBenefits"><span><Gift size={17} aria-hidden="true"/>Thoughtful wishes</span><span><Users size={17} aria-hidden="true"/>Shared celebrations</span><span><ShieldCheck size={17} aria-hidden="true"/>Clear contributions</span></div>
  </section>;
}
