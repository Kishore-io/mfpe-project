package com.cts.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cts.exception.DrugQuantityNotAvailable;
import com.cts.exception.InvalidTokenException;
import com.cts.exception.SubscriptionIDNotFoundException;
import com.cts.feign.AuthFeign;
import com.cts.feign.DrugClient;
import com.cts.feign.SubscriptionClient;
import com.cts.model.RefillOrder;
import com.cts.model.RefillOrderLine;
import com.cts.repo.RefillOrderRepository;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefillOrderServicelmpl implements IRefillOrder{

	@Autowired
	private RefillOrderRepository orepo;

	@Autowired
	private IRefillOrderSubscription subservice;

	@Autowired
	private SubscriptionClient subClient;

	@Autowired
	private DrugClient drugClient;
	
	@Autowired
	private AuthFeign authFeign;
	
	@Value("${Exception.message}")
	private String msg;
	//private String msg= "Invalid Credentials";

	@Override
	public List<RefillOrder> getStatus(long subID,String token) throws SubscriptionIDNotFoundException, com.cts.exception.InvalidTokenException {
		log.info("Inside getStatus");
		if (authFeign.getValidity(token).isValid()) {
			List<RefillOrder> list = new ArrayList<>();
			List<RefillOrder> flist = null;
			try {
				list = (ArrayList<RefillOrder>) orepo.findAll();
				flist = list.stream().filter(x -> x.getSubID() == subID).collect(Collectors.toList());
				flist.get(0);
			} catch (Exception e) {
				throw new SubscriptionIDNotFoundException("Invalid Subscription ID");
			}
			return flist;
		}
		else
			throw new InvalidTokenException(msg);
		

	}
	
	

	@Override
	public List<RefillOrderLine> getRefillDuesAsOfDate(String memberId, int date,String token) throws InvalidTokenException {
		log.info("Inside getRefillDuesAsOfDate");
		if (authFeign.getValidity(token).isValid()) {
			List<RefillOrderLine> list = subservice.getAllSubscription(token);
			List<RefillOrderLine> memberList = list.stream().filter(x -> x.getMemberID().equals(memberId))
					.collect(Collectors.toList());
			return memberList.stream().filter(x -> date % x.getRefillTime() != 0).collect(Collectors.toList());
		}
		else
			throw new InvalidTokenException(msg);
		}

	@Override
	public RefillOrder requestAdhocRefill(long policyId, long subID, String memberID, String location,int quantity,String token)
			throws ParseException, DrugQuantityNotAvailable, InvalidTokenException,FeignException {
		log.info("inside requestAdhocRefill");
		
		if (authFeign.getValidity(token).isValid()) {
			//ResponseEntity<String> drugrName = subClient.getDrugNameBySubscriptionId(token,memberID);
			String drugrName = subClient.getDrugNameBySubscriptionId(token,subID);
			String name = drugrName;
			ResponseEntity<?> updateDetail = drugClient.updateQuantity(token,name, location, quantity);
			int responseValue = updateDetail.getStatusCodeValue();
			if (responseValue == 200) {
				RefillOrder order = new RefillOrder();
				order.setSubID(subID);
				;
				Date date = new Date();
				DateFormat format = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
				String str = format.format(date);
				order.setRefillDate(format.parse(str));
				order.setQuantity(quantity);
				order.setMemberID(memberID);
				order.setPayStatus(true);
				orepo.save(order);
				return order;
			} else {
				throw new DrugQuantityNotAvailable("Drug Quantity Not Available");
			}
		}
		else
			throw new InvalidTokenException(msg);
		

	}

	
	
	@Override
	public RefillOrder requestRefill(long subId, int quantity, String memberId, String token)
			throws ParseException, InvalidTokenException {
		log.info("inside requestRefill method");

		if (authFeign.getValidity(token).isValid()) {
			RefillOrder refillOrder = new RefillOrder();
			refillOrder.setSubID(subId);
			Date date = new Date();
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
			String str = format.format(date);
			refillOrder.setRefillDate(format.parse(str));
			refillOrder.setQuantity(quantity);
			refillOrder.setPayStatus(true);
			refillOrder.setMemberID(memberId);
			orepo.save(refillOrder);

			return refillOrder;
		} else
			throw new InvalidTokenException(msg);
	}
	
	

	@Override
	public String updateRefill(String token) throws InvalidTokenException {
		log.info("inside UpdateRefill");
		
		if (authFeign.getValidity(token).isValid()) {
			List<RefillOrderLine> list = subservice.getAllSubscription(token);
			try {
				list.stream().forEach(x -> {
					Calendar cal = Calendar.getInstance();

					int minute = cal.get(Calendar.MINUTE);
					if (minute % x.getRefillTime() == 0) {


						try {
							requestRefill(x.getSubID(),x.getQuantity(),x.getMemberID(),token);
						} catch (ParseException | InvalidTokenException e) {
							log.info("Exception:", e.getMessage());
						}

					}
				}

				);

			} catch (Exception e) {
				log.info("Exception:", e.getMessage());
			}

			return "Successfully updated";
		}
		else
			throw new InvalidTokenException(msg);
		
	}



	@Override
	public boolean getRefillPaymentDues(long subID,String token) throws InvalidTokenException {
		log.info("inside getRefillDuesAsOfPayment");
		if (authFeign.getValidity(token).isValid()) {
			List<RefillOrder> list = orepo.findAll();
			List<RefillOrder> subList = list.stream().filter(x -> x.getSubID() == subID).collect(Collectors.toList());
			if (subList.isEmpty())
				return false;
			else {
				List<RefillOrder> paymentDue = subList.stream().filter(x -> (!x.getPayStatus()))
						.collect(Collectors.toList());
				if (paymentDue.isEmpty())
					return true;
			}

			return false;
		}
		else
			throw new InvalidTokenException(msg);
		
	}
	
}
