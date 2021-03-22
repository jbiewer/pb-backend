package com.piggybank.model;

public class BankAccount {
        public String bankName; 
        public long accountNo; 
        public long routingNo; 
        public String nameOnAccount;

        public void setBankName(String bankName) {
            this.bankName = bankName; 
        }

        public String getBankName() {
            return bankName; 
        }

        public void setAccountNumber(long accountNo) {
            this.accountNo = accountNo; 
        }

        public long getAccountNumber() {
            return accountNo; 
        }

        public void setRoutingNumber(long routingNo) {
            this.routingNo = routingNo; 
        }

        public long getRoutingNumber() {
            return routingNo; 
        }       
        
        public void setNameOnAccount(String nameOnAccount) {
            this.nameOnAccount = nameOnAccount; 
        }

        public String getNameOnAccount() {
            return nameOnAccount; 
        }

}