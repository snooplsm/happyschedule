<div>
<img src="http://i.imgur.com/OmQGY.png" alt="letters" width="30%" style="padding-right:3px"/>
<img src="http://i.imgur.com/DtnAm.png" alt="numbers" width="30%"/>
</div>
BUILD:

	mvn install source:jar


USAGE:

	JumpDialog d = new JumpDialog(this,new JumpListener() {

		@Override
		public void onJump(Character c) {

		}

	});

	d.show();

GOTCHAS:

It supports landscape and portrait but it is up to the user to maintain the changes.  We will provide an activity soon.

