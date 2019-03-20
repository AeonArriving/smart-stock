package smart.stock.spider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import smart.stock.constant.Constants;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @Auther: sunjx
 * @Date: 2019/3/18 0018 12:00
 * @Description: 爬取股票财务数据,保存到数据库
 */
@Slf4j
@Component
public abstract class StockFinanceSpider implements PageProcessor {

    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(3000)
            .setTimeOut(10000);

    @Override
    public void process(Page page) {
        Selectable tbody = page.getHtml().$("div#zaiyaocontent table.web2 tbody");
        if(null != tbody){
            Selectable trListSelectable = tbody.$("tr");
            if(null != trListSelectable){
                List<Selectable> trList = trListSelectable.nodes();
                if(!CollectionUtils.isEmpty(trList)){
                    for(Selectable tr: trList){
                        page.putField(tr.$("td.dotborder div.tishi").xpath("//strong/text()").toString(), tr.$("td:eq(1)").xpath("//div[@class='tishi']/text()").toString());
                    }
                }
            }
        }
    }

    //最新财务指标
    @Async
    public void fetch(String code, String year, int type){

        if(type > 0){
            run(getFinanceInfoTypes().getUrl(), code, year + Constants.FinanceDateTypes.getTextByKey(type), getStockFinancePipeline());
        }else{
            //时期类型小于等于0查全部
            for(Constants.FinanceDateTypes value : Constants.FinanceDateTypes.values()){
                run(getFinanceInfoTypes().getUrl(), code, year + value.getText(), getStockFinancePipeline());
            }
        }
    }

    protected abstract StockFinancePipeline getStockFinancePipeline();

    protected abstract Constants.FinanceInfoTypes getFinanceInfoTypes();

    private void run(String url, String code, String date, Pipeline pipeline){
        Spider.create(this)
                .addUrl(String.format(url, code, date))
                .thread(5)

                .addPipeline(pipeline)
                .run();
    }

    @Override
    public Site getSite() {
        return site;
    }

}
