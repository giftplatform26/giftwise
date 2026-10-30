package com.giftplatform.gift.retailer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

@Component
public class RetailerPageClient {
    static final int MAX_BYTES=2*1024*1024;
    public static URI validateUrl(String value) {
        try {
            URI uri=URI.create(value);
            String host=uri.getHost();
            if(value.length()>2048 || !"https".equalsIgnoreCase(uri.getScheme()) || host==null ||
               uri.getUserInfo()!=null || (uri.getPort()!=-1 && uri.getPort()!=443) ||
               !host.contains(".") || host.contains(":") || host.matches("[0-9.]+") ||
               host.toLowerCase().matches(".*\\.(localhost|local|internal|test|invalid|example)\\.?"))
                throw new IllegalArgumentException();
            return uri;
        } catch(Exception e) {throw new IllegalArgumentException("Enter a public retailer HTTPS URL");}
    }
    static boolean publicAddress(InetAddress address) {
        if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||
           address.isSiteLocalAddress()||address.isMulticastAddress()) return false;
        byte[] b=address.getAddress();int a=b[0]&255,c=b[1]&255;
        if(b.length==16) return (a&0xe0)==0x20 && !(a==0x20&&c==0x02) && !(a==0x20&&c==0x01&&(b[2]&255)==0&&(b[3]&255)==0); // Global unicast only; excludes mapped/local IPv6.
        return a!=0 && a!=10 && a!=127 && a<224 && !(a==100&&c>=64&&c<=127) &&
            !(a==169&&c==254) && !(a==172&&c>=16&&c<=31) && !(a==192&&(c==168||c==0)) &&
            !(a==198&&(c==18||c==19));
    }
    private record Resource(byte[] bytes,String type){}
    public String fetch(String source) throws IOException {
        Resource result=download(source,false);
        var charset=org.apache.hc.core5.http.ContentType.parse(result.type()).getCharset();
        return new String(result.bytes(),charset==null?StandardCharsets.UTF_8:charset);
    }
    public byte[] fetchImage(String source) throws IOException {return download(source,true).bytes();}
    private Resource download(String source,boolean image) throws IOException {
        URI uri=validateUrl(source);
        DnsResolver resolver=new DnsResolver(){
            public InetAddress[] resolve(String host) throws UnknownHostException {
                InetAddress[] addresses=InetAddress.getAllByName(host);
                for(InetAddress a:addresses)if(!publicAddress(a))throw new UnknownHostException("Retailer must resolve to public addresses");
                return addresses; // These validated addresses are used directly by the connection manager.
            }
            public String resolveCanonicalHostname(String host){return host;}
        };
        var manager=PoolingHttpClientConnectionManagerBuilder.create().setDnsResolver(resolver)
            .setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(4))
                .setSocketTimeout(Timeout.ofSeconds(6)).build()).build();
        try(var client=HttpClients.custom().setConnectionManager(manager).disableRedirectHandling()
            .disableAutomaticRetries().disableCookieManagement().setDefaultRequestConfig(RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(6)).setConnectionRequestTimeout(Timeout.ofSeconds(2)).build()).build()) {
            record Page(Resource resource,String redirect){}
            for(int redirects=0;redirects<4;redirects++) {
                HttpGet request=new HttpGet(uri);
                request.setHeader("User-Agent","GiftPlatformMetadata/1.0");
                request.setHeader("Accept",image?"image/jpeg,image/png,image/webp":"text/html,application/xhtml+xml");
                Page page=client.execute(request,response->{
                    if(response.getCode()>=300 && response.getCode()<400) {
                        var location=response.getFirstHeader("Location");
                        if(location==null)throw new IOException("Retailer redirect has no destination");
                        return new Page(null,location.getValue());
                    }
                    if(response.getCode()==403 || response.getCode()==429)
                        throw new IOException("This retailer blocks automated lookups. You can still save its link and enter the gift details.");
                    if(response.getCode()==404)throw new IOException("The retailer product page was not found. Check the product link.");
                    if(response.getCode()!=200)throw new IOException("The retailer returned HTTP "+response.getCode()+". Try its direct product page link.");
                    var entity=response.getEntity();
                    if(entity==null || entity.getContentType()==null ||
                        (image?!entity.getContentType().startsWith("image/"):!(entity.getContentType().contains("text/html")||entity.getContentType().contains("application/xhtml+xml"))))
                        throw new IOException("Retailer did not return HTML");
                    try(InputStream stream=entity.getContent()) {
                        // Product metadata is normally in the head. Parse a bounded prefix so
                        // large retailer pages do not fail solely because of their total size.
                        byte[] bytes=stream.readNBytes(image?2_000_001:MAX_BYTES);
                        if(image&&bytes.length>2_000_000)throw new IOException("Retailer photo exceeds 2 MB");
                        return new Page(new Resource(bytes,entity.getContentType()),null);
                    }
                });
                if(page.redirect()==null)return page.resource();
                uri=validateUrl(uri.resolve(page.redirect()).toString());
            }
            throw new IOException("Retailer redirected too many times; use the direct product URL");
        }
    }
}
