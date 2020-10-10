package com.example.miaosha1.controller;

import com.example.miaosha1.Service.GoodsService;
import com.example.miaosha1.Service.MiaoshaUserService;
import com.example.miaosha1.Service.RedisService;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.redis.GoodsKey;
import com.example.miaosha1.result.Result;
import com.example.miaosha1.vo.GoodsDetailVo;
import com.example.miaosha1.vo.GoodsVo;
import com.example.miaosha1.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody//缓存整个商品页面，粒度最大
    public String toLogin(HttpServletRequest request,HttpServletResponse response,
                          Model model, MiaoshaUser user) {//直接把user对象注入进来（把下面的整个过程抽取出来，因为很多地方都需要获取当前user）
        System.out.println("/goods/to_list");
        model.addAttribute("user",user);

        //return "goods_list";
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if(!StringUtils.isEmpty(html)){
            //System.out.println(html);
            return html;
        }

        //获取商品数据
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);

        //这里要将SpringWebContext修改为WebContext，在thymeleaf.spring5中已经删除了SpringWebContext
        //剔除了对 ApplicationContext 过多的依赖
        WebContext ctx =new WebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap());
        //手动渲染模板,放在缓存中（这里缓存的是整个页面）
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html))
            //将结果直接放在缓存中，不用每次获取时都让前端页面通过model中的数据进行渲染
            //因为使用缓存，得到的不是最新数据，设置过期时间为60秒
            redisService.set(GoodsKey.getGoodsList,"",html);

        //结果输出
        return html;

    }

    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody//缓存的是对应商品，根据不同商品，URL不同（key也不同），URL级缓存
    public String toDetail2(HttpServletRequest request,HttpServletResponse response,
                           Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId) {
        System.out.println("/goods/to_detail2");

        model.addAttribute("user",user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if(!StringUtils.isEmpty(html)){
            //System.out.println(html);
            return html;
        }

        //加入商品
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        //加入秒杀时间、秒杀状态
        long startDate = goods.getStartDate().getTime();
        long endDate = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        System.out.println(goods);

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if(now < startDate){
            miaoshaStatus=0;
            remainSeconds = (int) ((startDate-now)/1000);
        }else if(now > endDate){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        //return "goods_detail";
       
        //这里要将SpringWebContext修改为WebContext，在thymeleaf.spring5中已经删除了SpringWebContext
        //剔除了对 ApplicationContext 过多的依赖
        WebContext ctx =new WebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap());
        //手动渲染,放在缓存中（这里缓存的是整个页面）
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if(!StringUtils.isEmpty(html))
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);

        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}")//对应的url也要修改
    @ResponseBody//页面静态化，页面存在前端（将页面放在static），动态数据通过接口从服务端获取，服务端只需要写接口
    public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response,
                                          Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId) {
        System.out.println("/goods/detail");
        //加入商品
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        //加入秒杀时间、秒杀状态
        long startDate = goods.getStartDate().getTime();
        long endDate = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        System.out.println(goods);

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if(now < startDate){
            miaoshaStatus=0;
            remainSeconds = (int) ((startDate-now)/1000);
        }else if(now > endDate){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaUser(user);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);

        return Result.success(goodsDetailVo);
    }

}
