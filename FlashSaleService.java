package cn.edu.xmu.flashsale.service;



import cn.edu.xmu.flashsale.dao.FlashSaleDao;
import cn.edu.xmu.flashsale.model.bo.FlashSale;
import cn.edu.xmu.flashsale.model.vo.FlashSaleItemRetVo;
import cn.edu.xmu.flashsale.model.vo.FlashSaleItemVo;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleService {

    private Logger logger = LoggerFactory.getLogger(FlashSaleService.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private FlashSaleDao flashSaleDao;

    public ReturnObject createFlashSale(Long id, FlashSale bo)
    {
        return flashSaleDao.createFlashSale(id,bo);
    }

    public ReturnObject deleteFlashSale(Long id)
    {
        return flashSaleDao.deleteFlashSale(id);
    }

    public ReturnObject changeFlashSale(Long id, FlashSale bo)
    {
        return flashSaleDao.changeFlashSale(id,bo);
    }

    public ReturnObject onlineFlashSale(Long id)
    {
        return flashSaleDao.onlineFlashSale(id);
    }

    public ReturnObject offlineFlashSale(Long id)
    {
        return flashSaleDao.offlineFlashSale(id);
    }

    public ReturnObject createFlashSaleItem(Long id, FlashSaleItemVo vo)
    {
        return flashSaleDao.createFlashSaleItem(id,vo);
    }

    public ReturnObject deleteFlashSaleItem(Long fid,Long id)
    {
        return flashSaleDao.deleteFlashSaleItem(fid,id);
    }


    public Flux<FlashSaleItemRetVo> getFlashDetail(Long id) {
        return flashSaleDao.getFlashDetail(id);
    }

    public Flux<FlashSaleItemRetVo> getCurrentFlashDetail() {
        return flashSaleDao.getCurrentFlashDetail();
    }

    public ReturnObject removeFlashSale(Long id)
    {
        return flashSaleDao.removeFlashSale(id);
    }

}
