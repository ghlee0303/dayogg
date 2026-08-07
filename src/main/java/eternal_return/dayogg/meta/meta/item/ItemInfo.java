package eternal_return.dayogg.meta.meta.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.common.utils.ListUtils;
import eternal_return.dayogg.meta.enums.ItemEnum;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ItemInfo(
        String name,
        @JsonProperty("item_enum") ItemEnum itemEnum,
        Integer code,
        @JsonProperty("purchase") Map<PurchaseEnum, Purchase> purchaseMap
) {
    public enum PurchaseEnum {
        DRONE, LUMI, KIOSK
    }

    public ItemInfo withCode(Integer code) {
        return new ItemInfo(name, itemEnum, code, purchaseMap);
    }

    public record Purchase(
            Integer cost,
            Integer qty
    ) {
    }

    public int countTransferred(
            Function<PurchaseEnum, List<Integer>> getTransferredFunc
    ) {
        int sumCount = 0;

        for (ItemInfo.PurchaseEnum purchaseEnum : purchaseMap.keySet()) {
            ItemInfo.Purchase purchase = purchaseMap.get(purchaseEnum);

            List<Integer> transferred = getTransferredFunc.apply(purchaseEnum);
            sumCount += ListUtils.count(transferred, code) * purchase.qty();
        }

        return sumCount;
    }

    public Purchase transferred(
            Function<PurchaseEnum, List<Integer>> getTransferredFunc
    ) {
        int sumCost = 0;
        int sumQty = 0;

        for (ItemInfo.PurchaseEnum purchaseEnum : purchaseMap.keySet()) {
            ItemInfo.Purchase purchase = purchaseMap.get(purchaseEnum);

            List<Integer> transferred = getTransferredFunc.apply(purchaseEnum);

            int listCount = ListUtils.count(transferred, code);
            sumQty += listCount * purchase.qty();
            sumCost += listCount * purchase.cost();
        }

        return new Purchase(sumCost, sumQty);
    }
}
