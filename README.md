It is a standalone android application to demonstrate a simple auction application. User can register or login to the application, create auction and bid on other user's auctions. User can review the auctions he/she created, bid on auctions other users created, he/she can also see which auction he/she has won.

Target SDK: API 23 Min SDK: API 14

This application uses universal-image-loader, android-segmented, android ormlite library to serve image loading, segment widget, android ORM module.  It uses Material design and Design library to implement the UI. It also uses a fragment tab view to make it easy to navigate from one screen to another.

There are 4 entities (User, AuctionItem, UserBid, AuctionPhoto) to describe user, auction item, user’s bid on auction, auction photo.  When app starts, it’ll create a bot user which will quote a random number on auction automatically when a new auction is created.  Bot user email: admin@auctionapp.com, password: password

The application has following main screens:
Login Screen: This is the entry screen of app, to keep it simple it only has two input fields: email and password, which allow user to create a new account on the fly. When the email and password don’t have a match in the system, it will pop up an alert to ask if he/she wants to create a new account. If user chooses yes, the new account will be created, then takes user to the home screen, on which user can access most of the functions of this application.  On Login Screen, there is an option “remember me”, by checking this option, user won’t need to input email and password next time when he/she starts app, the app will automatically login to the system and navigate to home screen.
Home screen: This is the main screen of app (see the snapshot below), there are 3 fragments: Home, My Auctions, Won which display the available auctions, auctions owned by current user, auctions won by current user.
On Home fragment user can review the opening auctions, auctions will start soon,  the auctions user created, the auctions user have won. There is also a convenient float button which can let user create a new auction.  If there are some opening auctions listed on the home page, user can click any of them to look at the item detail, bid on this item if the user hasn’t made any quote on it.  
For those auctions which will start soon, user can review the auction detail and the time when the auction will start.

On My Auctions fragment user can review the auctions he/she submitted to the system. He/she can also review the bids others had made on his/her auction.
On Won fragment user can see the auction he/she has won.
Auction Item Detail screen: It displays the detail of one auction: name, description, photo, start price etc. User can bid on the auction created by other user. He/she must make a quote over the start price or the highest quote. User can only bid once.
Create Auction screen: User can create an auction to let other user bid on. It includes name, description, one auction photo, start price, start time, end time. Start time can be earlier than Now, but end time can’t be earlier than start time or Now.
Submit Auction screen: on this screen user can make a quote on auction other user created if this auction is still open.
BotService: This is a background service which will make a bid when a new auction is created.

 
