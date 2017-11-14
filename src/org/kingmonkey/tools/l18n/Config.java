package org.kingmonkey.tools.l18n;

import com.google.api.client.util.Key;

/**
 * TODO: add description
 *
 * @author Fernando Giovanini <fernando@teamkingmonkey.com>
 * @since 14.11.17
 */
public class Config
{
    @Key
    public String application_name;

    @Key
    public String file_id;

    @Key
    public String default_lang;

    @Key
    public String[] languages;

    @Key
    public String destination_folder;
}
