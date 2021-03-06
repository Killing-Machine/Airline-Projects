package com.airline.handlers;

import com.airline.bean.*;
import com.airline.dao.FlightMapper;
import com.airline.dao.FlightandorderMapper;
import com.airline.dao.PassengerMapper;
import com.airline.dao.UserMapper;
import com.airline.services.order.IOrderService;
import com.airline.services.payment.IPaymentService;
import com.airline.utils.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IPaymentService paymentService;
    @Autowired
    private FlightandorderMapper flightandorderMapper;

    @Autowired
    private FlightMapper flightMapper;


    @Autowired
    private PassengerMapper passengerMapper;

    @RequestMapping("/create_order.do")
    @ResponseBody
    public Msg createOrder(HttpServletRequest request, HttpSession session, Double amount, Date takeoffDate, @RequestParam(value = "flights_id") int[] flights_id) {
        //get the necessary data
        Passenger passenger = (Passenger) session.getAttribute("user");

        Order order = new Order();
        Paymentrecord payment = new Paymentrecord();
        payment.setAmount(amount.toString());
        //start to process
        Order orderCreated = orderService.createOrder(order, passenger, payment);

        // flightdandorder table insert
        Flightandorder flightandorder = new Flightandorder();
        flightandorder.setOrderid(orderCreated.getOrderid());
        for (int i :
                flights_id) {
            flightandorder.setFlightid(i);
            Flight flight = flightMapper.selectByPrimaryKey(i);
            takeoffDate.setHours(flight.getTakeofftime().getHours());
            takeoffDate.setMinutes(flight.getTakeofftime().getMinutes());
            takeoffDate.setSeconds(flight.getTakeofftime().getSeconds());
            flightandorder.setOrderid(order.getOrderid());
            flightandorder.setSeatnum(7);
            flightandorder.setSeattype("Economic");
            flightandorder.setTakeoffdate(takeoffDate);
            flightandorderMapper.insert(flightandorder);
        }
        return Msg.success().add("order", order);
    }

    @RequestMapping("/pay_order.do")
    public ModelAndView payOrder(@RequestParam(value = "orderid") String orderid,
                                 @RequestParam(value = "passagerid") String passagerid,
                                 @RequestParam(value = "paymentid") String paymentid,
                                 @RequestParam(value = "status") String status,
                                 @RequestParam(value = "date") String date
    ) {
        Order order = new Order();
        order.setOrderid(Integer.parseInt(orderid.trim()));
        order.setPassagerid(Integer.parseInt(passagerid.trim()));
        order.setPaymentid(Integer.parseInt(paymentid.trim()));
        order.setStatus(status.trim());
        order.setDate(new Date(Long.parseLong(date.trim())));
        ModelAndView modelAndView = new ModelAndView();
        Paymentrecord paymentrecord = paymentService.queryPayment(order.getPaymentid());
        modelAndView.addObject("order", order);
        modelAndView.addObject("payment", paymentrecord);
        modelAndView.addObject("date", order.getDate().getTime());
        modelAndView.setViewName("payOrder.jsp");
        return modelAndView;
    }

    @RequestMapping("/orderManagement.do")
    public ModelAndView orderManagement(@RequestParam(value = "user_id") String user_id) {

        PassengerExample passengerExample = new PassengerExample();
        passengerExample.or().andUseridEqualTo(Integer.valueOf(user_id));
        Passenger passenger = passengerMapper.selectByExample(passengerExample).get(0);

        List<Order> orderList = orderService.QueryOrdersByPassenger(passenger.getPassengerid());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("order_list", orderList);
        modelAndView.setViewName("order_management.jsp");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping("/refundOrder.do")
    public Msg refundOrder(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "order_id") String orderId) {
        request.setAttribute("order_id", orderId);
        if (paymentService.refund(request, response)) {
            return Msg.success().add("order_id", orderId)
                    .add("response", "The order is successfully refunded!")
                    .add("successful", "True");
        } else {
            return Msg.fail().add("order_id", orderId)
                    .add("response", "Failed to refunded the order!")
                    .add("successful", "False");
        }
    }


    @RequestMapping("/order_is_paid.do")
    public ModelAndView orderIsPaid(@RequestParam(value = "orderid") String orderid,
                                    @RequestParam(value = "passagerid") String passagerid,
                                    @RequestParam(value = "paymentid") String paymentid,
                                    @RequestParam(value = "status") String status,
                                    @RequestParam(value = "date") String date) {
        Order order = new Order();
        order.setOrderid(Integer.parseInt(orderid.trim()));
        order.setPassagerid(Integer.parseInt(passagerid.trim()));
        order.setPaymentid(Integer.parseInt(paymentid.trim()));
        order.setStatus(status.trim());
        order.setDate(new Date(Long.parseLong(date.trim())));
        ModelAndView modelAndView = new ModelAndView();
        order = orderService.orderIsPaid(order);
        Paymentrecord paymentrecord = paymentService.queryPayment(order.getPaymentid());
        paymentrecord = paymentService.paymentIsPaid(paymentrecord);
        modelAndView.addObject("order", order);
        modelAndView.addObject("payment", paymentrecord);
        modelAndView.setViewName("success.jsp");
        return modelAndView;
    }

    @RequestMapping("/cancel_order.do")
    public ModelAndView cancelOrder(Order order) {
        boolean result = orderService.cancelOrder(order);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("order", order);
        if (result) {

            modelAndView.setViewName("cancel_order_success.jsp");
            //todo success-> cancel success page
        } else {
            modelAndView.setViewName("cancel_order_failed.jsp");
            //todo success-> cancel success page
        }
        return modelAndView;
    }

}
