package com.hrm.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hrm.entry.Response;
import com.hrm.constant.Constant;
import com.hrm.pojo.*;
import com.hrm.service.*;
import com.hrm.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hrm.utils.JWTUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;


/**
 * @author zjw
 * @package backend
 * @Date 2021/7/11
 * @Time 23:30
 */

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/mall")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TradetrService tradetrService;

    /*
     * 更新购物车
     * */
    @RequestMapping(path = "/cart", method = {RequestMethod.POST})
    @ResponseBody
    public Object updateShoppingCart(@RequestHeader("token") String token, @RequestBody JSONObject jsonObj) {

//        创建响应类
        Response res = new Response();
        int user_id = JWTUtils.getUserId(token);
//        定义购物车对象数组
        ArrayList<Cart> cartObjList = new ArrayList<Cart>();
//        获取商品数组
        JSONArray jsonArray = jsonObj.getJSONArray("trolley");
        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int product_id = jsonObject.getIntValue("product_id");
            int buy_count = jsonObject.getIntValue("buy_count");
            int specification_id = jsonObject.getIntValue("specification_id");

            cartObjList.add(new Cart(user_id, product_id, buy_count, specification_id));
        }

        try {
            cartService.deleteProductsByUserId(user_id);
        } catch (Exception e) {
            e.printStackTrace();
            res.code = Constant.CODE_UPDATE_SHOPPING_CART;
            res.data.put("message", "更新购物车失败");
        }

        try {
            for (Cart cart : cartObjList) {
                Cart checkCart = cartService.checkCart(cart);
                if (checkCart == null) {
                    cartService.insertCart(cart);
                } else {
                    cartService.updateProduct(cart);
                }
            }
            res.code = Constant.CODE_SUCCESS;
            res.data.put("message", "更新购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.code = Constant.CODE_UPDATE_SHOPPING_CART;
            res.data.put("message", "更新购物车失败");
        }
        return res;
    }


    @RequestMapping(path = "/cart", method = {RequestMethod.GET})
    @ResponseBody
    public Object getShoppingCart(@RequestHeader("token") String token) {

        //创建响应类
        Response res = new Response();
        //在token里获取用户id
        int user_id = JWTUtils.getUserId(token);
        ID id = new ID(user_id, null, null, null);
        try {
            ArrayList<ShowShopping> shopping = cartService.entryCart(id);
            res.code = 0;
            res.data.put("message", "返回购物车成功");
            ArrayList<ShowShopping> showShoppingArrayList = new ArrayList<>();
            int sum = 0;    //记录总价格
            for (int i = 0; i < shopping.size(); i++) {
                ShowShopping showObj = shopping.get(i);
                sum = sum + showObj.getBuy_count() * showObj.getPrice();
                showShoppingArrayList.add(showObj);
            }
            res.data.put("trolley", showShoppingArrayList);
            res.data.put("sum", sum);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            res.code = 1;
            res.data.put("message", "返回购物车失败");
        }
        return res;
    }


    @RequestMapping("/cart/destroy")
    @ResponseBody
    public Object DeleteFromCart(HttpServletRequest request, @RequestBody ArrayList<Map<String, Integer>> cartList) {
        //        创建响应类
        Response res = new Response();

        String token = request.getHeader("token");

        int user_id = JWTUtils.getUserId(token);

        System.out.println("user_id  " + user_id);

        ArrayList<Cart> cartObjList = new ArrayList<Cart>();

        for (int i = 0; i < cartList.size(); i++) {
            cartObjList.add(new Cart(user_id, null, null, null));
        }
        System.out.println("----------------------------2");
        int controller = 0;
        int index = 0;
        for (Map<String, Integer> map : cartList) {
            for (String key : map.keySet()
            ) {
                if (controller == 2) {
                    index++;
                    controller = 0;
                }

                if (controller == 0)
                    cartObjList.get(index).setProduct_id(map.get(key));
                else if (controller == 1)
                    cartObjList.get(index).setSpecification_id(map.get(key));
                controller++;
            }
        }
        System.out.println("---------------------3");
        try {
            for (int i = 0; i < cartList.size(); i++) {
                int deleteProduct = cartService.deleteProduct(cartObjList.get(i));
                System.out.println("ssssssssssssssssssssssss" + deleteProduct);
            }
            res.code = 0;
            res.data.put("message", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.code = 1;
            res.data.put("message", "删除失败");
        }
        return res;
    }


    @RequestMapping(path = "/cart/settlement", method = {RequestMethod.GET})
    @ResponseBody
    public Object ClearCart(HttpServletRequest request) {
        //        创建响应类
        Response res = new Response();
        int user_id = JWTUtils.getUserId(request.getHeader("token"));
        ID id = new ID(user_id, null, null, null);
        int aliPay = 0;     //需要支付的总额
        int userMoney = balanceService.checkMoneyByID(user_id);     //查询用户的余额
        if (userMoney <= 0  ) {
            res.code = 1;
            res.data.put("message", "用户余额不足");
            return res;
        }
        System.out.println("-------------用户余额：  " + userMoney);

        try {
            ArrayList<ShowShopping> shopping = cartService.entryCart(id);

            for (ShowShopping showObj : shopping) {
                aliPay = aliPay + showObj.getBuy_count() * showObj.getPrice();
            }

            if (aliPay > userMoney) {
                res.code = 1;
                res.data.put("message", "用户余额不足");
                return res;
            }

            ArrayList<Transaction> transactionArrayList = new ArrayList<Transaction>();
            ArrayList<Tradeex> tradeexArrayList = new ArrayList<>();

            for (int i = 0; i < shopping.size(); i++) {
                ShowShopping showObj = shopping.get(i);
                //获取用户与购物车相关联表的信息
                int product_id = showObj.getProduct_id();
                int specification_id = showObj.getSpecification_id();
                String product_name = showObj.getProduct_name();
                int price = showObj.getPrice();
                int buy_count = showObj.getBuy_count();

                //获取当前时间
                String currentDatetime = DateUtils.getCurrentDatetime();

                //设置过期时间
                String dueDatetime = DateUtils.setDueDatetime(currentDatetime, buy_count);

                //创建用户交易流水信息
                tradeexArrayList.add(new Tradeex(null, product_id, user_id, specification_id, product_name, price, currentDatetime, buy_count));


                System.out.println("当前时间" + currentDatetime);
                System.out.println("过期时间" + dueDatetime);

                System.out.println("管理端交易流水信息" + tradeexArrayList.get(i).toString());

                //插入交易流水信息信息
                tradetrService.insertTrade(tradeexArrayList.get(i));
                //System.out.println("trade 返回值" + trade);

                //创建用户交易信息
                transactionArrayList.add(new Transaction(tradeexArrayList.get(i).getTrade_tr_id(), user_id, product_id, specification_id, product_name, currentDatetime, buy_count, currentDatetime));

                System.out.println("客户端交易流水信息" + transactionArrayList.get(i).toString());

                int i1 = transactionService.insertTransaction(transactionArrayList.get(i));

                //从用户购物车中删除
                int deleteProduct = cartService.deleteProduct(new Cart(user_id, product_id, buy_count, specification_id));
            }

            System.out.println("----------------------------4");

            try {
                balanceService.updateMoneyById(new Balance(user_id, userMoney - aliPay));
            } catch (Exception e) {
                e.printStackTrace();
                res.code = 1;
                res.data.put("message", "更新用户余额失败");
                return res;
            }
            res.code = 0;
            res.data.put("message", "购买成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.code = 1;
            res.data.put("message", "购买失败");
        }
        return res;
    }
}
