# POKEPHONE
A Java-based command-line Phone Store Management System that modernizes retail operations by replacing manual record-keeping with role-based access, full catalog browsing, stock monitoring, and file-based data persistence.

  FEATURES
Login Authentication
This feature manages access to the Pokephone by checking each user’s credentials against the stored account records. It distinguishes between four account types, and each account stores a username, password, and role:
• Admin Account: Has full control of the system. Administrators can create, edit and delete stocks,remove accounts and products, apply discounts, and view sales reports.
• Manager Account: Identifies managers and provides access to inventory maintenance functions, discounts, and sales reports.
• Staff Account: Identifies staff members and provides access to daily sales and inventory viewing and walk-in sales processing , including applying discounts.
• Customer Account: Identifies customers and provides access to browsing and purchasing functions.
       The feature serves as the system’s primary access point, ensuring that users enter only through their assigned accounts.


Main Dashboard
The Main Dashboard is the central menu of the system. After login, it displays the logged-in user’s role and shows only the options that role is allowed to use. This keeps the system organized and prevents unauthorized access to sensitive functions.








Phone Inventory
The Phone Inventory feature displays the Pokephone list of available phones along with their details, such as brand, model, price, and stock. It is available to all four roles and gives users a clear view of what the store currently offers.


Phone Purchase and Checkout
This feature processes the sale of the phones.
• Customer Purchase: Allows customers to select a phone and complete a purchase.
• Staff Sales Processing: Allows staff members to process sales for walk-in customers and apply promo discounts.
       Each transaction is recorded under the username of the person who made it, and the phone’s stock is updated afterward.


Sales Report
The Sales Report feature summarizes the store’s recorded transactions for the selected period. It is available only to managers and administrators and helps them review sales performance, identify the best-selling phones, and make informed decisions about pricing and restocking. Reports are generated from the transaction records stored in the system’s text files. The available report types are:
•Weekly Sales Report: Summarizes the sales made during the week.
•Monthly Sales Report: Summarizes the sales made during the month.
•Annual Sales Report: Summarizes the sales made during the year.
       Each report displays the phones sold, the quantity sold, and the total sales amount for the selected period. This feature aims to give managers and administrators an organized view of sales over time.


Customer Discount Management
The discount feature allows staff, manager, and admin to assign a discount percentage to registered accounts. The discount can be set with a maximum of 30%. Discounts are stored in a text file and linked to the customer's username.
