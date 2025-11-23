package com.example.finalproject.service;

import com.example.finalproject.dao.DigitalCodeDao;
import com.example.finalproject.dao.ProductDao;
import com.example.finalproject.dao.UserDao;
import com.example.finalproject.model.DigitalCode;
import com.example.finalproject.model.Product;
import com.example.finalproject.model.User;
import com.example.finalproject.util.EmailSender;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;




public class DigitalCodeService {

    private final DigitalCodeDao digitalCodeDao = new DigitalCodeDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();

    



    public String generateGiftCardCode() {
        String code;
        do {
            
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = String.format("GAME-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12)
            );
        } while (!isCodeUnique(code)); 

        return code;
    }

    



    public String generateDigitalCode() {
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = String.format("DIGI-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12)
            );
        } while (!isCodeUnique(code));

        return code;
    }

    


    private boolean isCodeUnique(String code) {
        
        
        return !digitalCodeDao.isCodeValid(code);
    }

    









    public boolean createAndSendCodes(int orderId, int orderItemId, int productId, int userId, int quantity) {
        try {
            
            Optional<Product> productOpt = productDao.getById(productId);
            if (productOpt.isEmpty()) {
                System.err.println("Product not found: " + productId);
                return false;
            }

            Product product = productOpt.get();

            
            Optional<User> userOpt = userDao.getUserById(userId);
            if (userOpt.isEmpty()) {
                System.err.println("User not found: " + userId);
                return false;
            }

            User user = userOpt.get();

            
            String codeType = product.isGiftCard() ? "GiftCard" : "DigitalDownload";

            
            StringBuilder codesMessage = new StringBuilder();
            for (int i = 0; i < quantity; i++) {
                
                String code = product.isGiftCard() ? generateGiftCardCode() : generateDigitalCode();

                
                DigitalCode digitalCode = new DigitalCode();
                digitalCode.setOrderId(orderId);
                digitalCode.setOrderItemId(orderItemId);
                digitalCode.setProductId(productId);
                digitalCode.setUserId(userId);
                digitalCode.setCode(code);
                digitalCode.setCodeType(codeType);
                digitalCode.setRedeemed(false);
                digitalCode.setSentAt(new Timestamp(System.currentTimeMillis()));

                
                if (product.isGiftCard()) {
                    digitalCode.setOriginalValue(product.getPrice());
                    digitalCode.setBalance(product.getPrice());
                }

                
                digitalCodeDao.insert(digitalCode);

                
                codesMessage.append(code);
                if (i < quantity - 1) {
                    codesMessage.append("\n");
                }
            }

            
            sendCodeEmail(user.getEmail(), product, codesMessage.toString(), quantity);

            System.out.println("✅ Successfully generated and sent " + quantity + " code(s) for product: " + product.getName());
            return true;

        } catch (Exception e) {
            System.err.println("Error creating and sending codes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    


    private void sendCodeEmail(String email, Product product, String codes, int quantity) {
        try {
            String subject;
            String htmlBody;

            
            String[] codeArray = codes.split("\n");
            StringBuilder codeBoxes = new StringBuilder();
            for (String code : codeArray) {
                codeBoxes.append(String.format("""
                    <div class="code-box">
                        <div class="code">%s</div>
                    </div>
                    """, code));
            }

            if (product.isGiftCard()) {
                
                subject = "Your Gaming Gift Card Code" + (quantity > 1 ? "s" : "") + " 🎁";

                htmlBody = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f5f7fa; }
                            .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                            .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center; }
                            .header h1 { margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; }
                            .header .icon { font-size: 48px; margin-bottom: 10px; }
                            .content { padding: 40px 30px; }
                            .code-box { background-color: #f8f9fa; border: 2px dashed #667eea; padding: 20px; text-align: center; border-radius: 8px; margin: 15px 0; }
                            .code { font-family: 'Courier New', monospace; font-size: 20px; font-weight: 700; color: #667eea; letter-spacing: 2px; }
                            .info-box { background-color: #fce4ec; border-left: 4px solid #c2185b; padding: 20px; margin: 20px 0; border-radius: 4px; }
                            .footer { background-color: #2c3e50; color: #ffffff; padding: 30px; text-align: center; font-size: 14px; }
                            h2 { color: #2c3e50; font-size: 24px; margin-top: 0; }
                            p { color: #6c757d; line-height: 1.6; font-size: 15px; }
                            ol { color: #6c757d; line-height: 1.8; }
                            .divider { height: 1px; background-color: #dee2e6; margin: 30px 0; }
                            .warning-text { color: #dc3545; font-weight: 600; }
                        </style>
                    </head>
                    <body>
                        <div class="email-container">
                            <div class="header">
                                <div class="icon">🎁</div>
                                <h1>Your Gift Card%s!</h1>
                            </div>
                            <div class="content">
                                <h2>Thank you for your purchase!</h2>

                                <p>Your <strong>%s</strong> gift card code%s %s ready:</p>

                                %s

                                <div class="info-box">
                                    <h4 style="margin: 0 0 15px 0; color: #c2185b;">🎯 How to Redeem Your Gift Card</h4>
                                    <ol style="margin: 10px 0; padding-left: 20px;">
                                        <li>Log in to your ShopEase account</li>
                                        <li>Add items to your cart</li>
                                        <li>Go to checkout</li>
                                        <li>Enter your gift card code</li>
                                        <li>The amount will be applied to your order</li>
                                    </ol>
                                </div>

                                <p style="background-color: #fff3cd; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107;">
                                    <strong>💝 Perfect as a Gift!</strong><br>
                                    This code can be used by you or shared with friends and family.
                                </p>

                                <div class="divider"></div>

                                <p style="text-align: center;" class="warning-text">
                                    ⚠️ IMPORTANT: Store this email safely - codes cannot be resent!
                                </p>

                                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                                    Happy Gaming! 🎮
                                </p>
                            </div>
                            <div class="footer">
                                <p style="margin: 0 0 10px 0; color: #ffffff;">
                                    <strong>ShopEase Gaming Store</strong>
                                </p>
                                <p style="margin: 0; color: #95a5a6; font-size: 12px;">
                                    © 2025 ShopEase. All rights reserved.
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """,
                    quantity > 1 ? "s" : "",
                    product.getName(),
                    quantity > 1 ? "s" : "",
                    quantity > 1 ? "are" : "is",
                    codeBoxes.toString()
                );
            } else {
                
                subject = "Your Digital Download Code" + (quantity > 1 ? "s" : "") + " - " + product.getName() + " 💿";

                htmlBody = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f5f7fa; }
                            .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                            .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center; }
                            .header h1 { margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; }
                            .header .icon { font-size: 48px; margin-bottom: 10px; }
                            .content { padding: 40px 30px; }
                            .code-box { background-color: #f8f9fa; border: 2px dashed #667eea; padding: 20px; text-align: center; border-radius: 8px; margin: 15px 0; }
                            .code { font-family: 'Courier New', monospace; font-size: 20px; font-weight: 700; color: #667eea; letter-spacing: 2px; }
                            .info-box { background-color: #e3f2fd; border-left: 4px solid #1976d2; padding: 20px; margin: 20px 0; border-radius: 4px; }
                            .footer { background-color: #2c3e50; color: #ffffff; padding: 30px; text-align: center; font-size: 14px; }
                            h2 { color: #2c3e50; font-size: 24px; margin-top: 0; }
                            p { color: #6c757d; line-height: 1.6; font-size: 15px; }
                            ol { color: #6c757d; line-height: 1.8; }
                            .divider { height: 1px; background-color: #dee2e6; margin: 30px 0; }
                            .warning-text { color: #dc3545; font-weight: 600; }
                        </style>
                    </head>
                    <body>
                        <div class="email-container">
                            <div class="header">
                                <div class="icon">💿</div>
                                <h1>Your Digital Download!</h1>
                            </div>
                            <div class="content">
                                <h2>Thank you for your purchase!</h2>

                                <p>Your digital download code%s for <strong>%s</strong>:</p>

                                %s

                                <div class="info-box">
                                    <h4 style="margin: 0 0 15px 0; color: #1976d2;">📥 How to Download</h4>
                                    <ol style="margin: 10px 0; padding-left: 20px;">
                                        <li>Log in to your ShopEase account</li>
                                        <li>Go to "My Digital Codes" section</li>
                                        <li>Find your code and click "Access Download"</li>
                                        <li>Follow the download instructions</li>
                                    </ol>
                                </div>

                                <div class="divider"></div>

                                <p style="text-align: center;" class="warning-text">
                                    ⚠️ IMPORTANT: Store this email safely - codes cannot be resent!
                                </p>

                                <p style="text-align: center; color: #6c757d; font-size: 14px;">
                                    Enjoy your game! 🎮
                                </p>
                            </div>
                            <div class="footer">
                                <p style="margin: 0 0 10px 0; color: #ffffff;">
                                    <strong>ShopEase Gaming Store</strong>
                                </p>
                                <p style="margin: 0; color: #95a5a6; font-size: 12px;">
                                    © 2025 ShopEase. All rights reserved.
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """,
                    quantity > 1 ? "s" : "",
                    product.getName(),
                    codeBoxes.toString()
                );
            }

            
            EmailSender.sendHtmlEmail(email, subject, htmlBody);
            System.out.println("📧 Modern HTML code email sent to: " + email);

        } catch (Exception e) {
            System.err.println("Error sending code email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    


    public java.util.List<DigitalCode> getUserCodes(int userId) {
        return digitalCodeDao.getCodesByUserId(userId);
    }

    


    public boolean redeemCode(String code) {
        if (digitalCodeDao.isCodeValid(code)) {
            digitalCodeDao.redeemCode(code);
            return true;
        }
        return false;
    }
}
