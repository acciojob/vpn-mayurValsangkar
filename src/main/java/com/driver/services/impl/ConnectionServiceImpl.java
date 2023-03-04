package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        //1.If the user is already connected to any service provider, throw "Already connected" exception.
        //2.Else if the countryName corresponds to the original country of the user, do nothing.
        //  This means that the user wants to connect to its original country, for which we do not require a connection.
        //  Thus, return the user as it is.
        //3.Else, the user should be subscribed under a serviceProvider having option to connect to the given country.
        //  If the connection can not be made (As user does not have a serviceProvider or serviceProvider does not have given country,
        //  throw "Unable to connect" exception.
        //  Else,establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and return the updated user.
        //  If multiple service providers allow you to connect to the country, use the service provider having smallest id.

        User user = userRepository2.findById(userId).get();
        if(user.getConnected()==true){
            throw new Exception("Already connected");
        }

        String userCountry = user.getOriginalCountry().toString();
        if(userCountry.equals(countryName)){
            return user;
        }

        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        if(serviceProviderList.isEmpty()){
            throw new Exception("Unable to connect");
        }

        List<ServiceProvider> availableServiceProvider = new ArrayList<>();

        boolean flag = false;
        for(ServiceProvider serviceProvider : serviceProviderList){

            if(serviceProvider.getCountryList().contains(countryName)){
                flag = true;
                availableServiceProvider.add(serviceProvider);
            }
        }

        if(flag==false){
            throw new Exception("Unable to connect");
        }


        ServiceProvider serviceProvider = null;
        for (ServiceProvider serviceProvider1 : availableServiceProvider) {
            if (serviceProvider == null || serviceProvider1.getId() < serviceProvider.getId()) {
                serviceProvider = serviceProvider1;
            }
        }

        CountryName countryName1 = CountryName.valueOf(countryName);
        String maskedIp = countryName1.toCode()+"."+serviceProvider+"."+userId;
        user.setMaskedIp(maskedIp);

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(serviceProvider);
        serviceProvider.getConnectionList().add(connection);
        connectionRepository2.save(connection);
        userRepository2.save(user);

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null,
        //update relevant attributes and return updated user.

        User user = new User();
        if(!user.getConnected()==true){
            throw new Exception("Already disconnected");
        }

        user.setConnected(false);
        user.setMaskedIp(null);
        user.getConnectionList().clear();
        userRepository2.save(user);

        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        //Establish a connection between sender and receiver users
        //To communicate to the receiver, sender should be in the current country of the receiver.
        //If the receiver is connected to a vpn, his current country is the one he is connected to.
        //If the receiver is not connected to vpn, his current country is his original country.
        //The sender is initially not connected to any vpn.
        // If the sender's original country does not match receiver's current country,
        //we need to connect the sender to a suitable vpn.
        // If there are multiple options, connect using the service provider having smallest id
        //If the sender's original country matches receiver's current country,
        //we do not need to do anything as they can communicate. Return the sender as it is.
        //If communication can not be established due to any reason, throw "Cannot establish communication" exception

        User sender = userRepository2.findById(senderId).get();

        User receiver = userRepository2.findById(receiverId).get();

        CountryName senderCountry = sender.getOriginalCountry().getCountryName();

        CountryName receiverCountry = null;

        if(receiver.getConnected()==true){

            String maskedIp =  receiver.getMaskedIp();
            String code = maskedIp.substring(0, 4);

            for (CountryName country : CountryName.values()) {
                if (country.toCode().equals(code)) {
                    receiverCountry = country;
                }
            }
        }else{

            receiverCountry = receiver.getOriginalCountry().getCountryName();
        }

        if(!senderCountry.equals(receiverCountry)){

            connect(senderId, receiverCountry.toString());
        }else {
            return sender;
        }

        throw new Exception("Cannot establish communication");
    }
}
