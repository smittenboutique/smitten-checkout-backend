package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CheckoutController {

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

@GetMapping("/api/product")
@ResponseBody
public Map<String, Object> getProduct(@RequestParam String id) throws Exception {

    String accessToken = System.getenv("SQUARE_ACCESS_TOKEN");

    com.squareup.square.SquareClient client =
        new com.squareup.square.SquareClient.Builder()
            .environment(com.squareup.square.Environment.SANDBOX)
            .bearerAuthCredentials(
                new com.squareup.square.authentication.BearerAuthModel.Builder(accessToken).build()
            )
            .build();

    var response = client.catalogApi().retrieveCatalogObject(id, true);

    var obj = response.getResult().getObject();

    Map<String, Object> product = new HashMap<>();

    product.put("id", obj.getId());
    product.put("name", obj.getItemData().getName());

    String imageUrl = "";

    if (obj.getItemData().getImageIds() != null &&
        !obj.getItemData().getImageIds().isEmpty()) {

        String imageId = obj.getItemData().getImageIds().get(0);

        var imageResponse = client.catalogApi()
            .retrieveCatalogObject(imageId, false);

        imageUrl = imageResponse.getResult()
            .getObject()
            .getImageData()
            .getUrl();
    }

    product.put("image", imageUrl);

    return product;
}
    @ResponseBody
    public Map<String, Object> getProduct(@RequestParam String id) {
        Map<String, Object> product = new HashMap<>();

        product.put("id", id);
        product.put("name", "Product " + id);
        product.put("image", "https://via.placeholder.com/100");

        return product;
    }

    @GetMapping("/fbcheckout")
    public String fbcheckout() {
        return "fbcheckout";
    }
}
