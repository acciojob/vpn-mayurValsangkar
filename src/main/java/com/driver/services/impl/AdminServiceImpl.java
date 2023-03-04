package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {

        Admin admin  = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);

        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {

        Admin admin = adminRepository1.findById(adminId).get();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);

        admin.getServiceProviders().add(serviceProvider);

        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{

        ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();

        String code = "";
        CountryName countryName1 = null;
        countryName = countryName.toUpperCase();


        if(CountryName.AUS.toString().equals(countryName)) {
            code = CountryName.AUS.toCode();
            countryName1 = CountryName.AUS;
        }

        else if (CountryName.CHI.toString().equals(countryName)){
            code = CountryName.CHI.toCode();
            countryName1 = CountryName.CHI;
        }

        else if(CountryName.IND.toString().equals(countryName)){
            code = CountryName.IND.toCode();
            countryName1 = CountryName.IND;
        }

        else if(CountryName.JPN.toString().equals(countryName)){
            code = CountryName.JPN.toCode();
            countryName1 = CountryName.JPN;
        }

        else if(CountryName.USA.toString().equals(countryName)){
            code = CountryName.USA.toCode();
            countryName1 = CountryName.USA;
        }

        else {
            throw new Exception("Country not found");
        }


        Country country = new Country();
        country.setCountryName(countryName1);
        country.setCode(code);
        //country.setUser(null);

        serviceProvider.getCountryList().add(country);

        serviceProviderRepository1.save(serviceProvider);
        //countryRepository1.save(country);
        return serviceProvider;
    }
}
